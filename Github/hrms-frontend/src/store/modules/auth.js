import { defineStore } from 'pinia';
import { login, logout, getCurrentUser } from '@/api/auth';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('auth_token') || '',
    userInfo: null,
    permissions: []
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    userName: (state) => state.userInfo?.name || localStorage.getItem('user_name') || ''
  },
  
  actions: {
    // 登录操作（真实API）
    async login(credentials) {
      try {
        console.log('🔄 Store: 开始登录', { username: credentials.username });
        
        // 调用真实的登录API
        const response = await login(credentials);
        
        console.log('✅ Store: 登录API响应成功', response);
        
        // 存储token和用户信息
        this.token = response.token;
        localStorage.setItem('auth_token', response.token);
        
        // 存储用户基本信息
        const userInfo = {
          username: response.username,
          role: response.role,
          name: response.name || response.username
        };
        
        this.userInfo = userInfo;
        localStorage.setItem('user_info', JSON.stringify(userInfo));
        localStorage.setItem('user_role', response.role);
        localStorage.setItem('user_name', userInfo.name);
        
        console.log('💾 Store: 用户信息已存储', userInfo);
        
        return true;
      } catch (error) {
        console.error('❌ Store: 登录失败', error);
        return false;
      }
    },
    
    // 获取用户信息（真实API）
    async getUserInfo() {
      try {
        console.log('🔄 Store: 获取用户详细信息');
        
        // 调用真实的API获取用户信息
        const userInfo = await getCurrentUser(this.token);
        
        console.log('✅ Store: 用户信息获取成功', userInfo);
        
        this.userInfo = userInfo;
        
        // 更新本地存储
        localStorage.setItem('user_info', JSON.stringify(userInfo));
        if (userInfo.name) {
          localStorage.setItem('user_name', userInfo.name);
        }
        
        return userInfo;
      } catch (error) {
        console.error('❌ Store: 获取用户信息失败', error);
        throw error;
      }
    },
    
    // 登出操作
    async logout() {
      try {
        console.log('🔄 Store: 开始登出');
        
        // 调用登出API（如果需要）
        try {
          await logout();
        } catch (error) {
          console.warn('⚠️ Store: 登出API调用失败，继续清理本地数据', error);
        }
        
        // 清理状态和本地存储
        this.token = '';
        this.userInfo = null;
        this.permissions = [];
        
        // 清理所有相关的localStorage项
        const keysToRemove = [
          'auth_token',
          'user_info',
          'user_role',
          'user_name',
          'user_employee_id'
        ];
        
        keysToRemove.forEach(key => {
          localStorage.removeItem(key);
        });
        
        console.log('✅ Store: 登出完成，本地数据已清理');
        
        return true;
      } catch (error) {
        console.error('❌ Store: 登出失败', error);
        return false;
      }
    },
    
    // 检查登录状态
    checkAuthStatus() {
      const token = localStorage.getItem('auth_token');
      const userInfo = localStorage.getItem('user_info');
      
      if (token && userInfo) {
        this.token = token;
        try {
          this.userInfo = JSON.parse(userInfo);
          return true;
        } catch (error) {
          console.error('用户信息解析失败:', error);
          this.clearAuth();
          return false;
        }
      }
      
      return false;
    },
    
    // 清理认证信息
    clearAuth() {
      this.token = '';
      this.userInfo = null;
      this.permissions = [];
      
      const keysToRemove = [
        'auth_token',
        'user_info',
        'user_role',
        'user_name',
        'user_employee_id'
      ];
      
      keysToRemove.forEach(key => {
        localStorage.removeItem(key);
      });
    }
  }
});