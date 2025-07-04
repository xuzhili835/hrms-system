/**
 * 调试辅助工具
 * 用于诊断登录、用户信息和API调用问题
 */

class DebugHelper {
  constructor() {
    this.logs = [];
    this.maxLogs = 100;
  }

  /**
   * 记录调试日志
   */
  log(category, message, data = null) {
    const logEntry = {
      timestamp: new Date().toISOString(),
      category,
      message,
      data: data ? JSON.parse(JSON.stringify(data)) : null
    };

    this.logs.push(logEntry);
    
    // 保持日志数量限制
    if (this.logs.length > this.maxLogs) {
      this.logs.shift();
    }

    // 输出到控制台
    const emoji = this.getCategoryEmoji(category);
    if (data) {
      console.log(`${emoji} [${category}] ${message}`, data);
    } else {
      console.log(`${emoji} [${category}] ${message}`);
    }
  }

  /**
   * 获取分类对应的emoji
   */
  getCategoryEmoji(category) {
    const emojiMap = {
      'LOGIN': '🔑',
      'USER_INFO': '👤',
      'API_CALL': '🌐',
      'STORAGE': '💾',
      'ERROR': '❌',
      'SUCCESS': '✅',
      'WARNING': '⚠️',
      'DEBUG': '🔍'
    };
    return emojiMap[category] || '📝';
  }

  /**
   * 检查用户登录状态
   */
  checkLoginStatus() {
    this.log('DEBUG', '开始检查登录状态');

    const authToken = localStorage.getItem('auth_token');
    const userName = localStorage.getItem('user_name');
    const userRole = localStorage.getItem('user_role');
    const userEmployeeId = localStorage.getItem('user_employee_id');
    const userInfo = localStorage.getItem('user_info');

    const status = {
      hasToken: !!authToken,
      tokenPreview: authToken ? `${authToken.substring(0, 20)}...` : null,
      userName,
      userRole,
      userEmployeeId,
      hasUserInfo: !!userInfo,
      userInfoValid: false
    };

    if (userInfo) {
      try {
        const parsedUserInfo = JSON.parse(userInfo);
        status.userInfoValid = true;
        status.userInfoData = parsedUserInfo;
        this.log('USER_INFO', '用户信息解析成功', parsedUserInfo);
      } catch (error) {
        this.log('ERROR', '用户信息解析失败', error.message);
      }
    }

    this.log('LOGIN', '登录状态检查完成', status);
    return status;
  }

  /**
   * 测试API权限
   */
  async testApiPermissions(employeeId) {
    this.log('API_CALL', '开始测试API权限', { employeeId });

    const testResults = {
      employeeDetail: { status: 'pending', error: null },
      salary: { status: 'pending', error: null },
      leave: { status: 'pending', error: null }
    };

    // 动态导入request模块
    try {
      const request = (await import('@/utils/request')).default;

      // 测试员工详细信息API
      try {
        const employeeResult = await request.get(`/employees/byEmpId/${employeeId}`);
        testResults.employeeDetail.status = 'success';
        testResults.employeeDetail.data = employeeResult;
        this.log('SUCCESS', '员工详细信息API测试成功', employeeResult);
      } catch (error) {
        testResults.employeeDetail.status = 'failed';
        testResults.employeeDetail.error = {
          status: error.response?.status,
          message: error.message,
          data: error.response?.data
        };
        this.log('ERROR', '员工详细信息API测试失败', testResults.employeeDetail.error);
      }

      // 测试薪资API
      try {
        const salaryResult = await request.get(`/employees/my-salary/${employeeId}`);
        testResults.salary.status = 'success';
        testResults.salary.data = salaryResult;
        this.log('SUCCESS', '薪资API测试成功', salaryResult);
      } catch (error) {
        testResults.salary.status = 'failed';
        testResults.salary.error = {
          status: error.response?.status,
          message: error.message,
          data: error.response?.data
        };
        this.log('ERROR', '薪资API测试失败', testResults.salary.error);
      }

      // 测试请假API
      try {
        const leaveResult = await request.get(`/leave-applications/employee/${employeeId}`);
        testResults.leave.status = 'success';
        testResults.leave.data = leaveResult;
        this.log('SUCCESS', '请假API测试成功', leaveResult);
      } catch (error) {
        testResults.leave.status = 'failed';
        testResults.leave.error = {
          status: error.response?.status,
          message: error.message,
          data: error.response?.data
        };
        this.log('ERROR', '请假API测试失败', testResults.leave.error);
      }

    } catch (error) {
      this.log('ERROR', '无法导入request模块', error.message);
    }

    this.log('API_CALL', 'API权限测试完成', testResults);
    return testResults;
  }

  /**
   * 修复用户信息显示问题
   */
  async fixUserInfoDisplay() {
    this.log('DEBUG', '开始修复用户信息显示问题');

    const loginStatus = this.checkLoginStatus();
    
    if (!loginStatus.hasToken) {
      this.log('ERROR', '用户未登录，无法修复');
      return { success: false, reason: '用户未登录' };
    }

    if (!loginStatus.userEmployeeId) {
      this.log('ERROR', '无员工ID，无法修复');
      return { success: false, reason: '无员工ID' };
    }

    try {
      // 重新获取员工信息
      const request = (await import('@/utils/request')).default;
      const employeeDetail = await request.get(`/employees/byEmpId/${loginStatus.userEmployeeId}`);
      
      if (employeeDetail && employeeDetail.name) {
        // 更新本地存储
        const updatedUserInfo = {
          employeeId: loginStatus.userEmployeeId,
          name: employeeDetail.name,
          department: employeeDetail.dept,
          position: employeeDetail.pos,
          role: 'employee'
        };

        localStorage.setItem('user_name', employeeDetail.name);
        localStorage.setItem('user_info', JSON.stringify(updatedUserInfo));

        this.log('SUCCESS', '用户信息修复成功', updatedUserInfo);
        
        // 刷新页面以应用更改
        setTimeout(() => {
          window.location.reload();
        }, 1000);

        return { 
          success: true, 
          userInfo: updatedUserInfo,
          message: '用户信息已修复，页面将在1秒后刷新'
        };
      } else {
        this.log('ERROR', '获取的员工信息无效', employeeDetail);
        return { success: false, reason: '员工信息无效' };
      }
    } catch (error) {
      this.log('ERROR', '修复用户信息时发生错误', error);
      return { success: false, reason: error.message };
    }
  }

  /**
   * 获取所有日志
   */
  getLogs(category = null) {
    if (category) {
      return this.logs.filter(log => log.category === category);
    }
    return this.logs;
  }

  /**
   * 清除日志
   */
  clearLogs() {
    this.logs = [];
    this.log('DEBUG', '日志已清除');
  }

  /**
   * 导出调试报告
   */
  exportDebugReport() {
    const report = {
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent,
      url: window.location.href,
      loginStatus: this.checkLoginStatus(),
      logs: this.logs,
      localStorage: {
        auth_token: localStorage.getItem('auth_token') ? '[存在]' : '[不存在]',
        user_name: localStorage.getItem('user_name'),
        user_role: localStorage.getItem('user_role'),
        user_employee_id: localStorage.getItem('user_employee_id'),
        user_info: localStorage.getItem('user_info') ? '[存在]' : '[不存在]'
      }
    };

    const blob = new Blob([JSON.stringify(report, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `hrms-debug-report-${Date.now()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    this.log('SUCCESS', '调试报告已导出');
  }
}

// 创建全局调试实例
const debugHelper = new DebugHelper();

// 将调试工具添加到全局window对象，方便在控制台中使用
if (typeof window !== 'undefined') {
  window.hrmsDebug = {
    // 检查登录状态
    checkLogin: () => debugHelper.checkLoginStatus(),
    
    // 测试API权限
    testApi: (employeeId) => debugHelper.testApiPermissions(employeeId || localStorage.getItem('user_employee_id')),
    
    // 修复用户信息
    fixUserInfo: () => debugHelper.fixUserInfoDisplay(),
    
    // 查看日志
    logs: (category) => debugHelper.getLogs(category),
    
    // 清除日志
    clearLogs: () => debugHelper.clearLogs(),
    
    // 导出报告
    exportReport: () => debugHelper.exportDebugReport(),
    
    // 完整的调试实例
    helper: debugHelper
  };

  console.log('🔧 HRMS调试工具已加载！在控制台中使用 window.hrmsDebug 进行调试');
  console.log('📋 可用命令:');
  console.log('  - window.hrmsDebug.checkLogin() // 检查登录状态');
  console.log('  - window.hrmsDebug.testApi() // 测试API权限');
  console.log('  - window.hrmsDebug.fixUserInfo() // 修复用户信息显示');
  console.log('  - window.hrmsDebug.logs() // 查看日志');
  console.log('  - window.hrmsDebug.exportReport() // 导出调试报告');
}

export default debugHelper; 