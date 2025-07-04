import { defineStore } from 'pinia';
import { employeeLogin as apiLogin, adminLogin as apiAdminLogin } from '@/api/auth';
import router from '@/router';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('auth_token') || null,
    userName: localStorage.getItem('user_name') || '',
    userRole: localStorage.getItem('user_role') || '',
    userId: localStorage.getItem('user_id') || '',
    userEmployeeId: localStorage.getItem('user_employee_id') || '',
    error: null,
    loading: false,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.userRole && state.userRole.toUpperCase() === 'ADMIN',
    isEmployee: (state) => state.userRole && state.userRole.toUpperCase() === 'EMPLOYEE',
  },

  actions: {
    setUserData({ token, name, role, user = {} }) {
      console.log('💾 开始存储用户数据:', { token: token?.substring(0, 20) + '...', name, role, user });
      
      // 验证关键数据
      if (!name) {
        console.error('❌ 警告: name字段为空');
      }
      
      if (user.employeeId && name === user.employeeId) {
        console.warn('⚠️ 警告: name与employeeId相同，可能是显示问题:', { name, employeeId: user.employeeId });
      }
      
      this.token = token;
      this.userName = name; // 这里存储的应该是真实姓名
      this.userRole = role;
      this.userId = user.id || user.employeeId || '';
      this.userEmployeeId = user.employeeId || '';
      
      // 存储到localStorage
      localStorage.setItem('auth_token', token);
      localStorage.setItem('user_name', name); // 确保存储的是真实姓名
      localStorage.setItem('user_role', role);
      localStorage.setItem('user_id', user.id || user.employeeId || '');
      localStorage.setItem('user_employee_id', user.employeeId || '');
      localStorage.setItem('user_info', JSON.stringify(user));
      
      console.log('✅ 用户信息已保存到localStorage:', {
        user_name: name,
        user_role: role,
        user_employee_id: user.employeeId,
        user_id: user.id || user.employeeId
      });
      
      // 验证存储结果
      const storedUserName = localStorage.getItem('user_name');
      const storedUserInfo = localStorage.getItem('user_info');
      
      console.log('🔍 存储验证:', {
        storedUserName,
        storedUserInfo: storedUserInfo ? JSON.parse(storedUserInfo) : null
      });
      
      if (storedUserName !== name) {
        console.error('❌ 存储验证失败: localStorage中的user_name与预期不符');
      }
    },

    clearUserData() {
      this.token = null;
      this.userName = '';
      this.userRole = '';
      this.userId = '';
      this.userEmployeeId = '';

      localStorage.removeItem('auth_token');
      localStorage.removeItem('user_name');
      localStorage.removeItem('user_role');
      localStorage.removeItem('user_id');
      localStorage.removeItem('user_employee_id');
      localStorage.removeItem('user_info');
    },

    async login(credentials) {
      this.loading = true;
      this.error = null;
      try {
        console.log('🚀 开始员工登录:', credentials.username);
        const response = await apiLogin(credentials.username, credentials.password);
        
        if (response.token && response.user) {
          console.log('✅ 登录成功，用户信息:', response.user);
          
          // 确保使用真实姓名而不是员工ID
          const displayName = response.user.name || response.user.employeeId;
          
          this.setUserData({
            token: response.token,
            name: displayName, // 使用真实姓名
            role: response.user.role,
            user: response.user
          });
          
          router.push('/employee/dashboard');
          return true;
        } else {
          throw new Error('登录响应数据不完整');
        }
      } catch (error) {
        console.error('❌ 员工登录失败:', error);
        this.error = error.message || '登录失败，请检查您的凭据。';
        return false;
      } finally {
        this.loading = false;
      }
    },

    async adminLogin(credentials) {
      this.loading = true;
      this.error = null;
      try {
        console.log('🚀 开始管理员登录:', credentials.username);
        const response = await apiAdminLogin(credentials.username, credentials.password);
        
        if (response.token && response.user) {
          console.log('✅ 管理员登录成功，用户信息:', response.user);
          
          this.setUserData({
            token: response.token,
            name: response.user.name || response.user.username,
            role: response.user.role,
            user: response.user
          });
          
          router.push('/admin/dashboard');
          return true;
        } else {
          throw new Error('登录响应数据不完整');
        }
      } catch (error) {
        console.error('❌ 管理员登录失败:', error);
        this.error = error.message || '管理员登录失败，请检查您的凭据。';
        return false;
      } finally {
        this.loading = false;
      }
    },

    logout() {
      this.clearUserData();
      // 清除所有路由历史记录
      window.history.replaceState(null, '', '/login');
      // 强制跳转到登录页面
      router.replace('/login').then(() => {
        // 确保页面完全刷新，清除所有组件状态
        window.location.reload();
      }).catch(() => {
        // 如果路由跳转失败，直接使用浏览器跳转
        window.location.href = '/login';
      });
    },
  },
}); 