/**
 * 管理员相关API接口
 */
import request from '@/utils/request';

/**
 * 获取仪表板统计数据
 */
export const getDashboardStats = async () => {
  try {
    console.log('🔍 获取仪表板统计数据...');
    
    // 添加时间戳参数防止缓存
    const timestamp = Date.now();
    const response = await request.get('/admins/dashboard-stats', {
      params: {
        _t: timestamp
      }
    });
    
    console.log('✅ 仪表板统计数据获取成功:', response);
    console.log('📊 获取到的待处理申请数量:', response.pendingApplications);
    
    return response;
  } catch (error) {
    console.error('❌ 获取仪表板统计数据失败:', error);
    throw error;
  }
};

/**
 * 获取月度数据趋势
 */
export const getMonthlyTrends = async () => {
  try {
    console.log('🔍 获取月度数据趋势...');
    const response = await request.get('/admins/monthly-trends');
    console.log('✅ 月度数据趋势获取成功:', response);
    return response;
  } catch (error) {
    console.error('❌ 获取月度数据趋势失败:', error);
    throw error;
  }
};

/**
 * 获取所有管理员列表
 */
export const getAllAdmins = async () => {
  try {
    console.log('🔍 获取所有管理员列表...');
    const response = await request.get('/admins');
    console.log('✅ 管理员列表获取成功:', response);
    return response;
  } catch (error) {
    console.error('❌ 获取管理员列表失败:', error);
    throw error;
  }
};

/**
 * 根据ID获取管理员信息
 */
export const getAdminById = async (id) => {
  try {
    console.log('🔍 获取管理员信息:', { id });
    const response = await request.get(`/admins/${id}`);
    console.log('✅ 管理员信息获取成功:', response);
    return response;
  } catch (error) {
    console.error('❌ 获取管理员信息失败:', error);
    throw error;
  }
};

/**
 * 添加新管理员
 */
export const addAdmin = async (adminData) => {
  try {
    console.log('➕ 添加新管理员:', adminData);
    const response = await request.post('/admins', adminData);
    console.log('✅ 管理员添加成功:', response);
    return response;
  } catch (error) {
    console.error('❌ 添加管理员失败:', error);
    throw error;
  }
};

/**
 * 更新管理员信息
 */
export const updateAdmin = async (id, adminData) => {
  try {
    console.log('🔄 更新管理员信息:', { id, adminData });
    const response = await request.put(`/admins/${id}`, adminData);
    console.log('✅ 管理员信息更新成功:', response);
    return response;
  } catch (error) {
    console.error('❌ 更新管理员信息失败:', error);
    throw error;
  }
};

/**
 * 获取当前管理员信息
 */
export const getCurrentAdmin = async () => {
  try {
    console.log('🔄 获取当前管理员信息');
    
    // 获取本地存储的管理员信息
    const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}');
    const storedUsername = localStorage.getItem('user_name');
    const username = storedUsername || userInfo.username || 'admin';
    
    // 通过用户名获取管理员详细信息
    const response = await request.get('/admins/search', {
      params: { username: username }
    });
    
    if (response && response.length > 0) {
      const adminData = response[0];
      console.log('✅ 获取管理员信息成功:', adminData);
      
      return {
        id: adminData.id,
        username: adminData.username,
        role: adminData.role,
        createdAt: adminData.created_at || adminData.createdAt
      };
    }
    
    // 如果API获取失败，返回默认信息
    console.warn('⚠️ 未能从API获取管理员信息，使用默认信息');
    return {
      username: username,
      role: 'ADMIN',
      createdAt: null
    };
    
  } catch (error) {
    console.error('❌ 获取管理员信息失败:', error);
    
    // API调用失败时，返回本地存储的基本信息
    const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}');
    const storedUsername = localStorage.getItem('user_name');
    
    return {
      username: storedUsername || userInfo.username || 'admin',
      role: userInfo.role || 'ADMIN',
      createdAt: null
    };
  }
};

/**
 * 管理员修改密码 - 参考员工密码修改逻辑
 */
export const changeAdminPassword = async (passwordData) => {
  try {
    console.log('🔄 管理员开始修改密码:', passwordData);
    
    // 获取当前管理员用户名
    const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}');
    const storedUsername = localStorage.getItem('user_name');
    const username = storedUsername || userInfo.username || 'admin';
    
    // 首先验证原密码是否正确
    try {
      await request.post('/auth/login', {
        username: username,
        password: passwordData.oldPassword
      });
      console.log('✅ 管理员原密码验证成功');
    } catch (error) {
      console.error('❌ 管理员原密码验证失败:', error);
      if (error.response?.status === 401) {
        throw new Error('原密码错误，请重新输入');
      }
      throw new Error('密码验证失败，请重试');
    }
    
    // 原密码验证成功后，调用专门的管理员修改密码接口
    await request.put('/admins/change-password', {
      newPassword: passwordData.newPassword
    });
    
    console.log('✅ 管理员密码修改成功');
    return { message: '密码修改成功' };
    
  } catch (error) {
    console.error('❌ 管理员修改密码失败:', error);
    
    // 如果是已知错误，直接抛出
    if (error.message.includes('原密码错误') || error.message.includes('密码验证失败')) {
      throw error;
    }
    
    // 检查其他错误类型
    if (error.response?.status === 403) {
      throw new Error('权限不足，无法修改密码');
    } else if (error.response?.status === 404) {
      throw new Error('管理员不存在');
    } else if (error.response?.status === 400) {
      throw new Error('新密码格式不正确');
    }
    
    throw new Error('密码修改失败，请重试');
  }
};

/**
 * 删除管理员
 */
export const deleteAdmin = async (id) => {
  try {
    console.log('🗑️ 删除管理员:', { id });
    const response = await request.delete(`/admins/${id}`);
    console.log('✅ 管理员删除成功:', response);
    return response;
  } catch (error) {
    console.error('❌ 删除管理员失败:', error);
    throw error;
  }
};

/**
 * 搜索管理员
 */
export const searchAdmins = async (params) => {
  try {
    console.log('🔍 搜索管理员:', params);
    const response = await request.get('/admins/search', { params });
    console.log('✅ 管理员搜索成功:', response);
    return response;
  } catch (error) {
    console.error('❌ 搜索管理员失败:', error);
    throw error;
  }
}; 