import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import performanceMonitor from './performance';

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求计数器，防止请求过于频繁
let requestCount = 0;
let requestResetTimer = null;
const MAX_REQUESTS_PER_SECOND = 10;

// 错误重试机制
const MAX_RETRY_COUNT = 3;
const RETRY_DELAY = 1000;

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 性能监控 - 请求频率控制
    if (!performanceMonitor.throttleRequests()) {
      return Promise.reject(new Error('请求频率过高，请稍后重试'));
    }

    // 添加请求计数
    requestCount++;
    
    // 重置计数器
    if (requestResetTimer) {
      clearTimeout(requestResetTimer);
    }
    requestResetTimer = setTimeout(() => {
      requestCount = 0;
    }, 1000);

    // 检查请求频率
    if (requestCount > MAX_REQUESTS_PER_SECOND) {
      console.warn('⚠️ 请求频率过高，已限制请求');
      return Promise.reject(new Error('请求频率过高，请稍后重试'));
    }

    // 添加认证token - 修复JWT格式
    const token = localStorage.getItem('auth_token');
    if (token && !config.url?.includes('/auth/login')) {
      // 确保token格式正确
      const formattedToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
      config.headers.Authorization = formattedToken;
      
      console.log('🔐 添加认证头:', {
        url: config.url,
        hasToken: !!token,
        tokenPreview: token.substring(0, 20) + '...'
      });
    }

    // 添加用户身份信息到请求头（用于后端权限验证）
    const userRole = localStorage.getItem('user_role');
    const userEmployeeId = localStorage.getItem('user_employee_id');
    
    if (userRole) {
      config.headers['X-User-Role'] = userRole;
    }
    
    if (userEmployeeId) {
      config.headers['X-User-Employee-Id'] = userEmployeeId;
    }

    // 添加请求唯一标识，防止重复请求
    config.metadata = {
      startTime: performance.now(),
      requestId: `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    };

    console.log(`🚀 API请求: ${config.method?.toUpperCase()} ${config.url}`, {
      requestId: config.metadata.requestId,
      hasAuth: !!config.headers.Authorization,
      userRole,
      userEmployeeId
    });

    return config;
  },
  (error) => {
    console.error('❌ 请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const config = response.config;
    const duration = performance.now() - (config.metadata?.startTime || 0);
    
    console.log(`✅ API响应: ${config.method?.toUpperCase()} ${config.url}`, {
      requestId: config.metadata?.requestId,
      duration: `${duration.toFixed(2)}ms`,
      status: response.status
    });

    // 检查响应时间
    if (duration > 5000) {
      console.warn('⏰ API响应较慢:', `${duration.toFixed(2)}ms`);
    }

    return response.data;
  },
  async (error) => {
    const config = error.config;
    const duration = performance.now() - (config.metadata?.startTime || 0);
    
    console.error(`❌ API错误: ${config?.method?.toUpperCase()} ${config?.url}`, {
      requestId: config?.metadata?.requestId,
      duration: `${duration.toFixed(2)}ms`,
      error: error.message,
      status: error.response?.status
    });

    // 避免在已经处理错误的情况下重复处理
    if (config._isRetrying) {
      return Promise.reject(error);
    }

    // 处理不同类型的错误
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          // 未授权 - 清除认证信息并跳转到登录页
          if (!window.location.href.includes('/login')) {
            localStorage.removeItem('auth_token');
            localStorage.removeItem('user_role');
            localStorage.removeItem('user_name');
            localStorage.removeItem('user_info');
            
            ElMessage.error('登录已过期，请重新登录');
            
            // 防止在登录页时重复跳转
            if (!router.currentRoute.value.path.includes('/login')) {
              router.push('/login');
            }
          }
          break;
          
        case 403:
          ElMessage.error('权限不足');
          break;
          
        case 404:
          ElMessage.error('请求的资源不存在');
          break;
          
        case 429:
          // 请求过于频繁
          ElMessage.warning('请求过于频繁，请稍后重试');
          break;
          
        case 500:
        case 502:
        case 503:
        case 504:
          // 服务器错误 - 尝试重试
          if (!config.retry) config.retry = 0;
          
          if (config.retry < MAX_RETRY_COUNT) {
            config.retry++;
            config._isRetrying = true;
            
            console.log(`🔄 重试请求 (${config.retry}/${MAX_RETRY_COUNT}):`, config.url);
            
            return new Promise(resolve => {
              setTimeout(() => {
                resolve(request(config));
              }, RETRY_DELAY * config.retry);
            });
          } else {
            ElMessage.error('服务器暂时不可用，请稍后重试');
          }
          break;
          
        default:
          const errorMessage = data?.message || error.message || '请求失败';
          ElMessage.error(errorMessage);
      }
    } else if (error.request) {
      // 网络错误
      if (error.message.includes('timeout')) {
        ElMessage.error('请求超时，请检查网络连接');
      } else if (error.message.includes('Network Error')) {
        ElMessage.error('网络连接失败，请检查网络');
      } else {
        ElMessage.error('网络错误，请稍后重试');
      }
    } else {
      // 其他错误
      if (!error.message.includes('请求频率过高')) {
        ElMessage.error(error.message || '未知错误');
      }
    }

    return Promise.reject(error);
  }
);

export default request;