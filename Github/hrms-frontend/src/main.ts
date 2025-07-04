import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
// @ts-ignore
import router from './router'
// @ts-ignore
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// @ts-ignore
import performanceMonitor from './utils/performance'
// @ts-ignore
import debugHelper from './utils/debugHelper'
// @ts-ignore
import userInfoFixer from './utils/userInfoFixer'

// 全局错误处理
const errorHandler = (err: unknown, instance: any, info: string) => {
  const error = err as Error;
  console.error('❌ Vue全局错误:', {
    error: error.message,
    stack: error.stack,
    component: instance?.$options.name || 'Unknown',
    info
  });
  
  // 如果是内存相关错误，清理资源
  if (error.message && (error.message.includes('Memory') || error.message.includes('maximum call stack'))) {
    performanceMonitor.clearAllTimers();
    console.warn('🧹 检测到内存问题，已清理所有定时器');
  }
};

// 全局未捕获异常处理
window.addEventListener('error', (event) => {
  console.error('❌ 全局未捕获异常:', {
    message: event.message,
    filename: event.filename,
    lineno: event.lineno,
    colno: event.colno,
    error: event.error
  });
});

// 全局Promise拒绝处理
window.addEventListener('unhandledrejection', (event) => {
  console.error('❌ 未处理的Promise拒绝:', {
    reason: event.reason,
    promise: event.promise
  });
  // 可以选择阻止默认行为
  // event.preventDefault();
});

// 创建Vue应用
const app = createApp(App);

// 设置全局错误处理器
app.config.errorHandler = errorHandler;

// 开发环境配置
if (import.meta.env.DEV) {
  app.config.performance = true;
}

// 全局属性
app.config.globalProperties.$performanceMonitor = performanceMonitor;

// 应用插件
app.use(router)
   .use(store)
   .use(ElementPlus)

// 启动性能监控
performanceMonitor.startMonitoring();

// 启动用户信息监控
userInfoFixer.startMonitoring();

// 启动调试工具
debugHelper.log('DEBUG', 'HRMS应用启动', {
  version: '2.2',
  environment: import.meta.env.MODE,
  timestamp: new Date().toISOString()
});

app.mount('#app');

console.log('🚀 HRMS应用已启动');
console.log('🔧 调试工具已就绪，使用 window.hrmsDebug 进行调试');
console.log('🔧 用户信息修复工具已就绪，使用 window.userInfoFixer 进行修复');
console.log('📊 性能监控已启动');
