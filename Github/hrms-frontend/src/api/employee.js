/**
 * 员工管理相关API接口
 */
import request from '@/utils/request';

/**
 * 获取员工列表 (管理员权限)
 */
export const getEmployeeList = async (params = {}) => {
  try {
    let url = '/employees';
    const queryParams = new URLSearchParams();
    
    // 添加搜索参数
    if (params.name) {
      queryParams.append('name', params.name);
    }
    if (params.dept) {
      queryParams.append('dept', params.dept);
    }
    
    // 如果有搜索参数，使用搜索接口
    if (queryParams.toString()) {
      url = `/employees/search?${queryParams.toString()}`;
    }
    
    const response = await request.get(url);
    const employees = response || [];
    
    // 客户端分页处理
    const page = params.page || 1;
    const pageSize = params.pageSize || 10;
    const startIndex = (page - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    
    let filteredEmployees = [...employees];
    
    // 按状态筛选
    if (params.status) {
      filteredEmployees = filteredEmployees.filter(emp => 
        emp.status === params.status
      );
    }
    
    const paginatedEmployees = filteredEmployees.slice(startIndex, endIndex);
    
    return {
      data: paginatedEmployees,
      total: filteredEmployees.length,
      page,
      pageSize
    };
  } catch (error) {
    console.error('获取员工列表失败:', error);
    throw error;
  }
};

/**
 * 获取员工详情
 */
export const getEmployeeDetail = async (empId) => {
  try {
    const response = await request.get(`/employees/byEmpId/${empId}`);
    return response;
  } catch (error) {
    console.error('获取员工详情失败:', error);
    throw error;
  }
};

/**
 * 根据员工ID获取员工信息（别名方法）
 */
export const getEmployeeById = async (empId) => {
  return getEmployeeDetail(empId);
};

/**
 * 添加员工 (管理员权限)
 */
export const addEmployee = async (employeeData) => {
  try {
    // 数据字段映射：确保与后端DTO字段一致
    const requestData = {
      empId: employeeData.empId,
      name: employeeData.name,
      dept: employeeData.dept,
      pos: employeeData.pos,
      pwd: employeeData.pwd,
      role: employeeData.role || 'EMPLOYEE',
      entryTime: employeeData.entryTime,
      status: employeeData.status || '在职'
    };
    
    const response = await request.post('/employees', requestData);
    return response;
  } catch (error) {
    console.error('添加员工失败:', error);
    throw error;
  }
};

/**
 * 更新员工信息 (管理员权限)
 */
export const updateEmployee = async (empId, employeeData) => {
  try {
    // 数据字段映射：确保与后端DTO字段一致
    const requestData = {
      name: employeeData.name,
      dept: employeeData.dept,
      pos: employeeData.pos,
      role: employeeData.role,
      entryTime: employeeData.entryTime,
      status: employeeData.status
    };
    
    // 过滤掉undefined的字段
    Object.keys(requestData).forEach(key => {
      if (requestData[key] === undefined) {
        delete requestData[key];
      }
    });
    
    await request.put(`/employees/${empId}`, requestData);
    return { message: '更新成功' };
  } catch (error) {
    console.error('更新员工失败:', error);
    throw error;
  }
};

/**
 * 删除员工 (仅限离职员工，管理员权限)
 */
export const deleteEmployee = async (empId) => {
  try {
    await request.delete(`/employees/${empId}/resigned`);
    return { message: '删除成功' };
  } catch (error) {
    console.error('删除员工失败:', error);
    throw error;
  }
};

/**
 * 批量删除员工
 */
export const batchDeleteEmployees = async (empIds) => {
  try {
    // 由于后端没有批量删除接口，这里逐个删除
    const promises = empIds.map(empId => deleteEmployee(empId));
    await Promise.all(promises);
    return { message: '批量删除成功' };
  } catch (error) {
    console.error('批量删除员工失败:', error);
    throw error;
  }
};

/**
 * 导出员工数据
 */
export const exportEmployees = async (params = {}) => {
  try {
    // 获取要导出的员工列表
    const result = await getEmployeeList(params);
    return result.data;
  } catch (error) {
    console.error('导出员工数据失败:', error);
    throw error;
  }
};

/**
 * 获取部门列表
 */
export const getDepartmentList = async () => {
  try {
    // 后端可能没有专门的部门接口，从员工列表中提取部门
    const employees = await request.get('/employees');
    const departments = [...new Set(employees.map(emp => emp.dept).filter(Boolean))];
    return departments.map(dept => ({ label: dept, value: dept }));
  } catch (error) {
    console.error('获取部门列表失败:', error);
    // 返回默认部门列表
    return [
      { label: '技术部', value: '技术部' },
      { label: '人事部', value: '人事部' },
      { label: '财务部', value: '财务部' },
      { label: '市场部', value: '市场部' },
      { label: '销售部', value: '销售部' },
      { label: '产品部', value: '产品部' },
      { label: '行政部', value: '行政部' },
      { label: '研发部', value: '研发部' }
    ];
  }
};

/**
 * 获取职位列表
 */
export const getPositionList = async () => {
  try {
    // 后端可能没有专门的职位接口，从员工列表中提取职位
    const employees = await request.get('/employees');
    const positions = [...new Set(employees.map(emp => emp.pos).filter(Boolean))];
    return positions.map(pos => ({ label: pos, value: pos }));
  } catch (error) {
    console.error('获取职位列表失败:', error);
    // 返回默认职位列表
    return [
      { label: 'Java开发工程师', value: 'Java开发工程师' },
      { label: '前端开发工程师', value: '前端开发工程师' },
      { label: '测试工程师', value: '测试工程师' },
      { label: '产品经理', value: '产品经理' },
      { label: '人事专员', value: '人事专员' },
      { label: '招聘经理', value: '招聘经理' },
      { label: '会计', value: '会计' },
      { label: '财务经理', value: '财务经理' },
      { label: '市场营销专员', value: '市场营销专员' }
    ];
  }
};

/**
 * 获取员工薪资信息 (员工本人权限)
 */
export const getEmployeeSalary = async (empId) => {
  try {
    const response = await request.get(`/employees/my-salary/${empId}`);
    return response;
  } catch (error) {
    console.error('获取薪资信息失败:', error);
    throw error;
  }
};

/**
 * 员工更新自己的个人信息
 */
export const updateMyProfile = async (empId, profileData) => {
  try {
    console.log('🔄 员工更新个人信息:', { empId, profileData });
    
    // 数据字段映射 - 只允许修改基本信息
    const requestData = {
      name: profileData.name
      // 员工只能修改姓名，其他字段由管理员管理
    };
    
    // 过滤掉undefined的字段
    Object.keys(requestData).forEach(key => {
      if (requestData[key] === undefined) {
        delete requestData[key];
      }
    });
    
    console.log('📤 发送更新请求:', requestData);
    
    // 使用员工专用的更新接口
    await request.put(`/employees/my-profile/${empId}`, requestData);
    
    console.log('✅ 个人信息更新成功');
    return { message: '个人信息更新成功' };
  } catch (error) {
    console.error('❌ 更新个人信息失败:', error);
    
    // 提供更详细的错误信息
    if (error.response?.status === 403) {
      throw new Error('权限不足，只能修改自己的信息');
    } else if (error.response?.status === 404) {
      throw new Error('员工信息不存在');
    } else {
      throw new Error(error.response?.data?.message || '更新失败');
    }
  }
};

/**
 * 员工修改密码
 */
export const changeEmployeePassword = async (empId, passwordData) => {
  try {
    console.log('🔄 员工修改密码:', { empId });
    
    const requestData = {
      pwd: passwordData.newPassword
    };
    
    await request.put(`/employees/change-password/${empId}`, requestData);
    
    console.log('✅ 密码修改成功');
    return { message: '密码修改成功' };
  } catch (error) {
    console.error('❌ 修改密码失败:', error);
    
    if (error.response?.status === 403) {
      throw new Error('权限不足，只能修改自己的密码');
    } else if (error.response?.status === 404) {
      throw new Error('员工信息不存在');
    } else {
      throw new Error(error.response?.data?.message || '密码修改失败');
    }
  }
}; 