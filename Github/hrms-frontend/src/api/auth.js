/**
 * 认证相关API接口
 */
import request from '@/utils/request';

/**
 * 员工登录
 */
export const employeeLogin = async (employeeId, password) => {
  console.log('🚀 开始员工登录:', { employeeId });
  
  try {
    const response = await request.post('/auth/login', {
      username: employeeId,
      password: password
    });
    
    console.log('✅ 登录API响应:', response);
    const { token, role, username, user } = response;
    
    if (role !== 'employee') {
      throw new Error('此账号不是员工账号，请使用员工登录入口');
    }
    
    // 检查是否返回了完整的用户信息
    if (user && user.name) {
      console.log('✅ 后端返回了完整的用户信息:', user);
      
      const userInfo = {
        employeeId: user.empId || username,
        name: user.name, // 使用后端返回的真实姓名
        department: user.dept || '',
        position: user.pos || '',
        role: 'employee',
        isFirstLogin: false
      };
      
      console.log('👤 构建的用户信息对象:', userInfo);
      
      return {
        token: token,
        user: userInfo
      };
    } else {
      // 后端没有返回完整用户信息，回退到原有逻辑
      console.warn('⚠️ 后端没有返回完整用户信息，使用原有逻辑获取');
      
      // 先存储token，再获取员工详细信息
      console.log('💾 先存储token以便后续API调用');
      localStorage.setItem('auth_token', token);
      localStorage.setItem('user_role', role);
      localStorage.setItem('user_employee_id', username);
      
      console.log('🔍 开始获取员工详细信息:', { username });
      
      try {
        const employeeDetail = await request.get(`/employees/byEmpId/${username}`);
        console.log('✅ 员工详细信息获取成功:', employeeDetail);
        
        // 验证获取到的信息
        if (!employeeDetail || !employeeDetail.name) {
          console.error('❌ 员工详细信息中缺少name字段:', employeeDetail);
          throw new Error('员工详细信息不完整，缺少姓名信息');
        }
        
        // 确保name字段是真实姓名而不是员工ID
        const realName = employeeDetail.name;
        if (realName === username) {
          console.warn('⚠️ 警告: 获取到的姓名与员工ID相同，可能是数据问题');
        }
        
        const userInfo = {
          employeeId: username,
          name: realName, // 确保使用数据库中的真实姓名
          department: employeeDetail.dept || '',
          position: employeeDetail.pos || '',
          role: 'employee',
          isFirstLogin: false
        };
        
        console.log('👤 构建的用户信息对象:', userInfo);
        
        return {
          token: token,
          user: userInfo
        };
      } catch (detailError) {
        console.error('❌ 获取员工详细信息失败:', detailError);
        
        // 清除可能不完整的存储信息
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user_role');
        localStorage.removeItem('user_employee_id');
        
        throw new Error('无法获取员工详细信息，请稍后重试或联系管理员');
      }
    }
  } catch (error) {
    console.error('❌ 员工登录失败:', error);
    
    // 统一错误处理
    if (error.response?.status === 401) {
      throw new Error('工号或密码错误');
    }
    throw error;
  }
};

/**
 * 管理员登录
 */
export const adminLogin = async (username, password) => {
  try {
    const response = await request.post('/auth/login', {
      username: username,
      password: password
    });
    
    // 解析响应数据
    console.log('管理员登录响应数据:', response);
    const { token, role, username: loginUsername, user } = response;
    
    // 角色检查 - 支持大小写不敏感
    if (role && role.toLowerCase() !== 'admin') {
      throw new Error('此账号不是管理员账号，请使用管理员登录入口');
    }
    
    // 检查是否返回了完整的用户信息
    if (user && user.name) {
      console.log('✅ 后端返回了完整的管理员信息:', user);
      
      return {
        token: token,
        user: {
          id: user.id || 1,
          username: user.username || loginUsername,
          name: user.name, // 使用后端返回的真实姓名
          role: role || 'ADMIN' // 保持后端返回的原始角色值
        }
      };
    } else {
      // 后端没有返回完整用户信息，使用默认值
      console.warn('⚠️ 后端没有返回完整管理员信息，使用默认值');
      
      return {
        token: token,
        user: {
          id: 1, // 后端可能没有返回id，使用默认值
          username: loginUsername,
          name: loginUsername, // 后端可能没有返回name，使用username代替
          role: role || 'ADMIN' // 保持后端返回的原始角色值
        }
      };
    }
  } catch (error) {
    console.error('管理员登录失败:', error);
    // 统一错误处理
    if (error.response?.status === 401) {
      throw new Error('用户名或密码错误');
    }
    throw error;
  }
};

/**
 * 员工注册 - 注意：后端可能没有注册接口，这里保留原有逻辑
 */
export const employeeRegister = async (userData) => {
  try {
    // 后端可能没有注册接口，如果有，可以调用
    // const response = await request.post('/api/auth/register', userData);
    
    // 暂时抛出错误，提示联系管理员
    throw new Error('员工注册功能暂不可用，请联系系统管理员添加员工信息');
  } catch (error) {
    console.error('员工注册失败:', error);
    throw error;
  }
};

/**
 * 修改密码 - 支持验证原密码
 */
export const changePassword = async (passwordData) => {
  try {
    console.log('🔄 开始修改密码:', passwordData);
    
    // 获取当前用户信息
    const currentUser = JSON.parse(localStorage.getItem('user_info') || '{}');
    const empId = currentUser.employeeId || currentUser.empId;
    
    if (!empId) {
      throw new Error('无法获取当前用户信息，请重新登录');
    }
    
    // 首先验证原密码是否正确
    try {
      await request.post('/auth/login', {
        username: empId,
        password: passwordData.oldPassword
      });
      console.log('✅ 原密码验证成功');
    } catch (error) {
      console.error('❌ 原密码验证失败:', error);
      if (error.response?.status === 401) {
        throw new Error('原密码错误，请重新输入');
      }
      throw new Error('密码验证失败，请重试');
    }
    
    // 原密码验证成功后，使用专门的修改密码接口
    await request.put(`/employees/change-password/${empId}`, {
      pwd: passwordData.newPassword
    });
    
    console.log('✅ 密码修改成功');
    return { message: '密码修改成功' };
    
  } catch (error) {
    console.error('❌ 修改密码失败:', error);
    
    // 如果是已知错误，直接抛出
    if (error.message.includes('原密码错误') || error.message.includes('密码验证失败')) {
      throw error;
    }
    
    // 检查其他错误类型
    if (error.response?.status === 403) {
      throw new Error('权限不足，无法修改密码');
    } else if (error.response?.status === 404) {
      throw new Error('用户不存在');
    } else if (error.response?.status === 400) {
      throw new Error('新密码格式不正确');
    }
    
    throw new Error('密码修改失败，请重试');
  }
};

/**
 * 获取当前用户信息
 */
export const getCurrentUser = async (token) => {
  try {
    // 从token或本地存储获取用户信息
    const userInfo = localStorage.getItem('user_info');
    if (userInfo) {
      const user = JSON.parse(userInfo);
      
      // 如果是员工，可以通过API获取最新信息
      if (user.role === 'employee' && user.employeeId) {
        try {
          const employeeData = await request.get(`/employees/byEmpId/${user.employeeId}`);
          return {
            ...user,
            name: employeeData.name,
            department: employeeData.dept,
            position: employeeData.pos,
            status: employeeData.status
          };
        } catch (error) {
          console.warn('获取员工详细信息失败，使用本地缓存信息:', error);
          return user;
        }
      }
      
      return user;
    }
    
    throw new Error('未找到用户信息');
  } catch (error) {
    console.error('获取当前用户信息失败:', error);
    throw error;
  }
};

/**
 * 退出登录
 */
export const logout = async () => {
  try {
    // 清除本地存储的认证信息
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_info');
    
    // 后端可能没有专门的登出接口，这里只做本地清理
    return { message: '退出登录成功' };
  } catch (error) {
    console.error('退出登录失败:', error);
    // 即使失败也要清除本地信息
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_info');
    throw error;
  }
};

/**
 * 验证Token有效性
 */
export const validateToken = async (token) => {
  try {
    // 可以通过调用需要认证的接口来验证token
    // 这里使用获取当前用户信息来验证
    await getCurrentUser(token);
    return true;
  } catch (error) {
    console.error('Token验证失败:', error);
    return false;
  }
};
