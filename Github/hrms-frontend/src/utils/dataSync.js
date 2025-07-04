/**
 * 前后端数据同步验证工具
 */
import request from './request';

class DataSyncValidator {
  constructor() {
    this.syncQueue = []; // 同步队列
    this.syncStatus = new Map(); // 同步状态追踪
    this.retryCount = 3; // 重试次数
    this.retryDelay = 1000; // 重试延迟（毫秒）
  }

  /**
   * 验证数据操作是否成功同步到后端
   */
  async validateOperation(operationType, data, apiCall) {
    const operationId = `${operationType}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    console.log(`🔄 开始验证数据操作: ${operationType}`, {
      operationId,
      data: this.maskSensitiveData(data)
    });

    try {
      // 执行API调用
      const result = await apiCall();
      
      // 等待一段时间确保数据库更新完成
      await this.waitForDbSync();
      
      // 验证操作结果
      const isValid = await this.verifyOperation(operationType, data, result);
      
      if (isValid) {
        console.log(`✅ 数据操作验证成功: ${operationType}`, {
          operationId,
          result: this.maskSensitiveData(result)
        });
        
        this.syncStatus.set(operationId, {
          status: 'success',
          timestamp: Date.now(),
          data: result
        });
        
        return result;
      } else {
        throw new Error('数据同步验证失败');
      }
    } catch (error) {
      console.error(`❌ 数据操作验证失败: ${operationType}`, {
        operationId,
        error: error.message
      });
      
      this.syncStatus.set(operationId, {
        status: 'failed',
        timestamp: Date.now(),
        error: error.message
      });
      
      throw error;
    }
  }

  /**
   * 等待数据库同步完成
   */
  async waitForDbSync(delay = 500) {
    return new Promise(resolve => {
      setTimeout(resolve, delay);
    });
  }

  /**
   * 验证具体操作类型
   */
  async verifyOperation(operationType, originalData, result) {
    switch (operationType) {
      case 'CREATE_EMPLOYEE':
        return this.verifyEmployeeCreation(originalData, result);
      
      case 'UPDATE_EMPLOYEE':
        return this.verifyEmployeeUpdate(originalData, result);
      
      case 'DELETE_EMPLOYEE':
        return this.verifyEmployeeDeletion(originalData, result);
      
      case 'CREATE_SALARY':
        return this.verifySalaryCreation(originalData, result);
      
      case 'CREATE_LEAVE_APPLICATION':
        return this.verifyLeaveApplicationCreation(originalData, result);
      
      default:
        console.warn(`⚠️ 未知操作类型: ${operationType}`);
        return true; // 未知操作默认认为成功
    }
  }

  /**
   * 验证员工创建
   */
  async verifyEmployeeCreation(originalData, result) {
    try {
      if (!originalData.empId) {
        throw new Error('员工ID不能为空');
      }

      // 查询刚创建的员工数据
      const employee = await request.get(`/employees/byEmpId/${originalData.empId}`);
      
      return employee && 
             employee.name === originalData.name &&
             employee.dept === originalData.dept &&
             employee.pos === originalData.pos;
    } catch (error) {
      console.error('验证员工创建失败:', error);
      return false;
    }
  }

  /**
   * 验证员工更新
   */
  async verifyEmployeeUpdate(originalData, result) {
    try {
      if (!originalData.empId) {
        throw new Error('员工ID不能为空');
      }

      // 查询更新后的员工数据
      const employee = await request.get(`/employees/byEmpId/${originalData.empId}`);
      
      // 验证更新的字段
      let isValid = true;
      if (originalData.name && employee.name !== originalData.name) isValid = false;
      if (originalData.dept && employee.dept !== originalData.dept) isValid = false;
      if (originalData.pos && employee.pos !== originalData.pos) isValid = false;
      
      return isValid;
    } catch (error) {
      console.error('验证员工更新失败:', error);
      return false;
    }
  }

  /**
   * 验证员工删除
   */
  async verifyEmployeeDeletion(originalData, result) {
    try {
      // 尝试查询被删除的员工，应该返回404或空结果
      try {
        await request.get(`/employees/byEmpId/${originalData.empId}`);
        return false; // 如果还能查到，说明删除失败
      } catch (error) {
        if (error.response?.status === 404) {
          return true; // 404说明删除成功
        }
        throw error;
      }
    } catch (error) {
      console.error('验证员工删除失败:', error);
      return false;
    }
  }

  /**
   * 验证薪资记录创建
   */
  async verifySalaryCreation(originalData, result) {
    try {
      // 查询员工的薪资记录
      const salaries = await request.get(`/salaries/employee/${originalData.empId}`);
      
      // 检查是否有匹配的薪资记录
      const matchingSalary = salaries.find(salary => 
        salary.salMonth === originalData.salMonth &&
        Math.abs(salary.totalSal - originalData.totalSal) < 0.01
      );
      
      return !!matchingSalary;
    } catch (error) {
      console.error('验证薪资创建失败:', error);
      return false;
    }
  }

  /**
   * 验证请假申请创建
   */
  async verifyLeaveApplicationCreation(originalData, result) {
    try {
      // 查询员工的请假记录
      const applications = await request.get(`/leave-applications/employee/${originalData.empId}`);
      
      // 检查是否有匹配的请假记录
      const matchingApplication = applications.find(app => 
        app.type === originalData.type &&
        app.startDate === originalData.startDate &&
        app.endDate === originalData.endDate
      );
      
      return !!matchingApplication;
    } catch (error) {
      console.error('验证请假申请创建失败:', error);
      return false;
    }
  }

  /**
   * 脱敏敏感数据用于日志输出
   */
  maskSensitiveData(data) {
    if (!data || typeof data !== 'object') return data;
    
    const masked = { ...data };
    
    // 脱敏密码字段
    if (masked.pwd || masked.password) {
      masked.pwd = '***';
      masked.password = '***';
    }
    
    // 脱敏身份证号
    if (masked.idCard) {
      masked.idCard = masked.idCard.replace(/(.{6})(.*)(.{4})/, '$1****$3');
    }
    
    return masked;
  }

  /**
   * 获取同步状态统计
   */
  getSyncStatistics() {
    const stats = {
      total: this.syncStatus.size,
      success: 0,
      failed: 0,
      pending: 0
    };

    this.syncStatus.forEach(status => {
      stats[status.status]++;
    });

    return stats;
  }

  /**
   * 清理过期的同步状态记录
   */
  cleanupOldRecords(maxAge = 24 * 60 * 60 * 1000) { // 24小时
    const now = Date.now();
    const toDelete = [];

    this.syncStatus.forEach((status, id) => {
      if (now - status.timestamp > maxAge) {
        toDelete.push(id);
      }
    });

    toDelete.forEach(id => {
      this.syncStatus.delete(id);
    });

    console.log(`🧹 清理了 ${toDelete.length} 条过期同步记录`);
  }

  /**
   * 批量验证操作
   */
  async validateBatchOperations(operations) {
    const results = [];
    
    for (const operation of operations) {
      try {
        const result = await this.validateOperation(
          operation.type,
          operation.data,
          operation.apiCall
        );
        results.push({ success: true, result });
      } catch (error) {
        results.push({ success: false, error: error.message });
      }
    }
    
    const successCount = results.filter(r => r.success).length;
    console.log(`📊 批量操作完成: ${successCount}/${operations.length} 成功`);
    
    return results;
  }
}

// 创建全局实例
const dataSyncValidator = new DataSyncValidator();

// 定期清理过期记录
setInterval(() => {
  dataSyncValidator.cleanupOldRecords();
}, 60 * 60 * 1000); // 每小时清理一次

export default dataSyncValidator; 