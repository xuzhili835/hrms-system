/**
 * 请假管理相关API接口
 */
import request from '@/utils/request';

// 请假类型配置
const leaveTypes = [
  { value: '年假', label: '年假', maxDays: 15 },
  { value: '病假', label: '病假', maxDays: 30 },
  { value: '事假', label: '事假', maxDays: 10 },
  { value: '婚假', label: '婚假', maxDays: 3 },
  { value: '产假', label: '产假', maxDays: 98 },
  { value: '陪产假', label: '陪产假', maxDays: 15 },
  { value: '丧假', label: '丧假', maxDays: 3 }
];

/**
 * 获取请假申请列表（管理员）
 */
export const getLeaveApplications = async (params = {}) => {
  try {
    // 构建查询参数
    const queryParams = new URLSearchParams();
    if (params.empId) queryParams.append('empId', params.empId);
    if (params.type) queryParams.append('type', params.type);
    if (params.status) queryParams.append('status', params.status);
    if (params.startDate) queryParams.append('startDate', params.startDate);
    if (params.endDate) queryParams.append('endDate', params.endDate);
    if (params.page) queryParams.append('page', params.page);
    if (params.pageSize) queryParams.append('pageSize', params.pageSize);
    
    const response = await request.get(`/leave-applications?${queryParams.toString()}`);
    return response;
  } catch (error) {
    console.error('获取请假申请失败:', error);
    throw error;
  }
};

/**
 * 获取员工个人请假记录
 */
export const getEmployeeLeaveHistory = async (employeeId, params = {}) => {
  try {
    // 使用正确的后端接口：/leave-applications/my-applications/{empId}
    const response = await request.get(`/leave-applications/my-applications/${employeeId}`, {
      params: params
    });
    
    console.log('📅 获取员工请假记录成功:', {
      employeeId,
      recordCount: response?.length || 0
    });
    
    return {
      data: response || [],
      total: response?.length || 0
    };
  } catch (error) {
    console.error('❌ 获取员工请假记录失败:', error);
    
    // 如果是403错误，提供更详细的错误信息
    if (error.response?.status === 403) {
      throw new Error('权限不足：无法访问请假记录，请确认您的身份或联系管理员');
    }
    
    throw error;
  }
};

/**
 * 获取当前用户的请假记录（别名方法）
 */
export const getMyLeaveApplications = async () => {
  try {
    const empId = localStorage.getItem('user_employee_id');
    if (!empId) {
      throw new Error('无法获取用户员工ID');
    }
    
    const response = await getEmployeeLeaveHistory(empId);
    return response;
  } catch (error) {
    console.error('获取个人请假记录失败:', error);
    throw error;
  }
};

/**
 * 获取请假申请详情（员工查看自己的）
 */
export const getLeaveApplicationDetail = async (id) => {
  try {
    // 使用员工专用的详情查看接口
    const response = await request.get(`/leave-applications/my-applications/detail/${id}`);
    return response;
  } catch (error) {
    console.error('获取请假申请详情失败:', error);
    
    // 处理不同类型的错误
    if (error.response?.status === 403) {
      throw new Error('权限不足：只能查看自己的请假申请详情');
    } else if (error.response?.status === 404) {
      throw new Error('请假申请不存在或已被删除');
    }
    
    throw error;
  }
};

/**
 * 更新请假申请状态（管理员审批）
 */
export const updateLeaveApplication = async (id, updateData) => {
  try {
    console.log('🔄 更新请假申请:', { id, updateData });
    
    const response = await request.put(`/leave-applications/${id}`, updateData);
    
    console.log('✅ 请假申请更新成功:', response);
    return response;
  } catch (error) {
    console.error('❌ 更新请假申请失败:', error);
    throw error;
  }
};

/**
 * 删除请假申请（管理员）
 */
export const deleteLeaveApplication = async (id) => {
  try {
    await request.delete(`/leave-applications/${id}`);
    return { message: '请假申请删除成功' };
  } catch (error) {
    console.error('删除请假申请失败:', error);
    throw error;
  }
};

/**
 * 提交请假申请（员工）
 */
export const submitLeaveApplication = async (applicationData) => {
  try {
    console.log('🔄 提交请假申请:', applicationData);
    
    // 数据字段映射
    const requestData = {
      empId: applicationData.empId || applicationData.employeeId,
      type: applicationData.type || applicationData.leaveType,
      startDate: applicationData.startDate,
      endDate: applicationData.endDate,
      days: applicationData.days,
      reason: applicationData.reason,
      status: '待审批' // 默认状态，与数据库一致
    };
    
    const response = await request.post('/leave-applications', requestData);
    
    console.log('✅ 请假申请提交成功:', response);
    return response;
  } catch (error) {
    console.error('❌ 提交请假申请失败:', error);
    throw error;
  }
};

/**
 * 获取请假类型配置
 */
export const getLeaveTypes = async () => {
  try {
    // 返回本地配置的请假类型
    return leaveTypes;
  } catch (error) {
    console.error('获取请假类型失败:', error);
    throw error;
  }
};

/**
 * 获取请假统计数据
 */
export const getLeaveStatistics = async (params = {}) => {
  try {
    // 获取所有请假申请进行统计
    const result = await getLeaveApplications({ pageSize: 1000 });
    const applications = result.data || [];
    
    // 按时间范围筛选
    let filteredApplications = [...applications];
    
    if (params.startDate) {
      filteredApplications = filteredApplications.filter(app => 
        app.startDate >= params.startDate
      );
    }
    
    if (params.endDate) {
      filteredApplications = filteredApplications.filter(app => 
        app.endDate <= params.endDate
      );
    }
    
    // 计算统计数据
    const totalApplications = filteredApplications.length;
    const pendingCount = filteredApplications.filter(app => app.status === 'pending').length;
    const approvedCount = filteredApplications.filter(app => app.status === 'approved').length;
    const rejectedCount = filteredApplications.filter(app => app.status === 'rejected').length;
    
    // 按请假类型统计
    const typeStats = leaveTypes.map(type => {
      const typeApplications = filteredApplications.filter(app => app.type === type.value);
      return {
        type: type.value,
        count: typeApplications.length,
        approvedCount: typeApplications.filter(app => app.status === 'approved').length
      };
    });
    
    // 按部门统计（如果有部门信息）
    const departmentStats = [];
    
    return {
      totalApplications,
      pendingCount,
      approvedCount,
      rejectedCount,
      typeStats,
      departmentStats
    };
  } catch (error) {
    console.error('获取请假统计失败:', error);
    throw error;
  }
};

/**
 * 审批请假申请（管理员）
 */
export const approveLeaveApplication = async (id, approvalData) => {
  try {
    // 数据字段映射
    const requestData = {
      status: approvalData.status, // 'approved' 或 'rejected'
      approveReason: approvalData.approveReason || '',
      approver: approvalData.approver || '系统管理员',
      approveDate: new Date().toISOString().split('T')[0]
    };
    
    await request.put(`/leave-applications/${id}`, requestData);
    return { message: '审批完成' };
  } catch (error) {
    console.error('审批请假申请失败:', error);
    throw error;
  }
};

/**
 * 取消请假申请（员工本人）
 */
export const cancelLeaveApplication = async (id, employeeId) => {
  try {
    console.log('🔄 撤销请假申请:', { id, employeeId });
    
    // 使用员工专用的撤销接口
    const response = await request.delete(`/leave-applications/my-applications/${id}`);
    
    console.log('✅ 请假申请撤销成功:', response);
    return { message: response.data || '请假申请已撤销' };
  } catch (error) {
    console.error('❌ 撤销请假申请失败:', error);
    
    // 处理不同类型的错误
    if (error.response?.status === 403) {
      throw new Error('权限不足：只能撤销自己的请假申请');
    } else if (error.response?.status === 400) {
      throw new Error(error.response.data || '只能撤销待审批的请假申请');
    } else if (error.response?.status === 404) {
      throw new Error('请假申请不存在或已被删除');
    }
    
    throw error;
  }
};

/**
 * 批量审批请假申请（管理员）
 */
export const batchApproveLeaveApplications = async (ids, approvalData) => {
  try {
    // 逐个审批
    const promises = ids.map(id => approveLeaveApplication(id, approvalData));
    await Promise.all(promises);
    return { message: '批量审批完成' };
  } catch (error) {
    console.error('批量审批失败:', error);
    throw error;
  }
};

/**
 * 导出请假记录
 */
export const exportLeaveApplications = async (params = {}) => {
  try {
    // 获取要导出的请假记录
    const result = await getLeaveApplications(params);
    return result.data;
  } catch (error) {
    console.error('导出请假记录失败:', error);
    throw error;
  }
}; 