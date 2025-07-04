/**
 * 用户信息修复工具
 * 专门用于修复用户信息显示问题，确保显示真实姓名而不是员工ID
 */

import request from '@/utils/request';

class UserInfoFixer {
  constructor() {
    this.isFixing = false;
  }

  /**
   * 检查当前用户信息状态
   */
  checkCurrentUserInfo() {
    const status = {
      localStorage: {
        auth_token: localStorage.getItem('auth_token'),
        user_name: localStorage.getItem('user_name'),
        user_role: localStorage.getItem('user_role'),
        user_employee_id: localStorage.getItem('user_employee_id'),
        user_info: localStorage.getItem('user_info')
      },
      parsed: null
    };

    if (status.localStorage.user_info) {
      try {
        status.parsed = JSON.parse(status.localStorage.user_info);
      } catch (error) {
        console.error('用户信息解析失败:', error);
      }
    }

    console.log('🔍 当前用户信息状态:', status);
    return status;
  }

  /**
   * 强制重新获取并修复用户信息
   */
  async forceRefreshUserInfo() {
    if (this.isFixing) {
      console.log('⏳ 正在修复中，请稍候...');
      return { success: false, message: '正在修复中' };
    }

    this.isFixing = true;
    console.log('🔧 开始强制修复用户信息');

    try {
      // 1. 检查当前状态
      const currentStatus = this.checkCurrentUserInfo();
      
      if (!currentStatus.localStorage.auth_token) {
        throw new Error('用户未登录');
      }

      if (!currentStatus.localStorage.user_employee_id) {
        throw new Error('缺少员工ID信息');
      }

      // 2. 重新获取员工详细信息
      console.log('📡 重新获取员工详细信息...');
      const employeeId = currentStatus.localStorage.user_employee_id;
      
      const employeeDetail = await request.get(`/employees/byEmpId/${employeeId}`);
      console.log('✅ 获取到员工详细信息:', employeeDetail);

      // 3. 验证获取到的信息
      if (!employeeDetail || !employeeDetail.name) {
        throw new Error('员工详细信息不完整或无姓名字段');
      }

      // 4. 构建正确的用户信息对象
      const correctedUserInfo = {
        employeeId: employeeId,
        name: employeeDetail.name,  // 确保使用数据库中的name字段
        department: employeeDetail.dept || '',
        position: employeeDetail.pos || '',
        role: 'employee',
        isFirstLogin: false
      };

      console.log('✨ 构建的正确用户信息:', correctedUserInfo);

      // 5. 更新所有localStorage字段
      localStorage.setItem('user_name', employeeDetail.name);
      localStorage.setItem('user_info', JSON.stringify(correctedUserInfo));

      console.log('💾 localStorage已更新');

      // 6. 验证更新结果
      const verifyStatus = this.checkCurrentUserInfo();
      console.log('🔍 验证修复结果:', verifyStatus);

      this.isFixing = false;

      return {
        success: true,
        message: '用户信息修复成功',
        userInfo: correctedUserInfo,
        beforeFix: currentStatus,
        afterFix: verifyStatus
      };

    } catch (error) {
      console.error('❌ 用户信息修复失败:', error);
      this.isFixing = false;
      
      return {
        success: false,
        message: `修复失败: ${error.message}`,
        error: error
      };
    }
  }

  /**
   * 自动检测并修复用户信息问题
   */
  async autoFixUserInfo() {
    console.log('🤖 开始自动检测用户信息问题');

    const status = this.checkCurrentUserInfo();
    
    // 检查是否需要修复
    const needsFix = this.detectUserInfoIssues(status);
    
    if (needsFix.length === 0) {
      console.log('✅ 用户信息无需修复');
      return { success: true, message: '用户信息正常', needsFix: false };
    }

    console.log('⚠️ 检测到问题:', needsFix);
    
    // 执行自动修复
    const fixResult = await this.forceRefreshUserInfo();
    
    return {
      ...fixResult,
      needsFix: true,
      detectedIssues: needsFix
    };
  }

  /**
   * 检测用户信息中的问题
   */
  detectUserInfoIssues(status) {
    const issues = [];

    // 检查基本字段
    if (!status.localStorage.user_name) {
      issues.push('缺少user_name');
    } else if (status.localStorage.user_name === status.localStorage.user_employee_id) {
      issues.push('user_name与员工ID相同，可能是显示问题');
    }

    if (!status.localStorage.user_info) {
      issues.push('缺少user_info');
    } else if (status.parsed) {
      if (!status.parsed.name) {
        issues.push('user_info中缺少name字段');
      } else if (status.parsed.name === status.parsed.employeeId) {
        issues.push('user_info中name字段等于employeeId，可能是显示问题');
      }
    }

    if (!status.localStorage.user_employee_id) {
      issues.push('缺少user_employee_id');
    }

    return issues;
  }

  /**
   * 实时监控用户信息变化
   */
  startMonitoring() {
    console.log('👁️ 开始监控用户信息变化');
    
    // 监听localStorage变化
    window.addEventListener('storage', (e) => {
      if (e.key && e.key.startsWith('user_')) {
        console.log('📝 检测到用户信息变化:', {
          key: e.key,
          oldValue: e.oldValue,
          newValue: e.newValue
        });
        
        // 延迟检查，确保所有相关字段都已更新
        setTimeout(() => {
          this.checkCurrentUserInfo();
        }, 100);
      }
    });

    // 定期检查
    setInterval(() => {
      const status = this.checkCurrentUserInfo();
      const issues = this.detectUserInfoIssues(status);
      
      if (issues.length > 0) {
        console.warn('⚠️ 定期检查发现问题:', issues);
      }
    }, 30000); // 30秒检查一次
  }

  /**
   * 获取调试信息
   */
  getDebugInfo() {
    const status = this.checkCurrentUserInfo();
    const issues = this.detectUserInfoIssues(status);
    
    return {
      currentStatus: status,
      detectedIssues: issues,
      isFixing: this.isFixing,
      recommendations: this.getRecommendations(issues)
    };
  }

  /**
   * 获取修复建议
   */
  getRecommendations(issues) {
    const recommendations = [];
    
    if (issues.includes('缺少user_name') || issues.includes('user_name与员工ID相同，可能是显示问题')) {
      recommendations.push('执行 userInfoFixer.forceRefreshUserInfo() 重新获取用户信息');
    }
    
    if (issues.includes('缺少user_info')) {
      recommendations.push('重新登录或执行强制刷新');
    }
    
    if (issues.length === 0) {
      recommendations.push('用户信息正常，无需操作');
    }
    
    return recommendations;
  }
}

// 创建全局实例
const userInfoFixer = new UserInfoFixer();

// 添加到全局对象，方便调试
if (typeof window !== 'undefined') {
  window.userInfoFixer = userInfoFixer;
  
  console.log('🔧 用户信息修复工具已加载！');
  console.log('📋 可用命令:');
  console.log('  - window.userInfoFixer.checkCurrentUserInfo() // 检查当前状态');
  console.log('  - window.userInfoFixer.forceRefreshUserInfo() // 强制修复');
  console.log('  - window.userInfoFixer.autoFixUserInfo() // 自动检测和修复');
  console.log('  - window.userInfoFixer.getDebugInfo() // 获取调试信息');
}

export default userInfoFixer; 