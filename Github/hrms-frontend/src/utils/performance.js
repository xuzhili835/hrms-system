/**
 * 性能监控工具
 * 用于检测和防止内存泄漏、无限循环等性能问题
 */

class PerformanceMonitor {
  constructor() {
    this.memoryThreshold = 100 * 1024 * 1024; // 100MB内存阈值
    this.cycleDetection = new Map(); // 循环检测
    this.intervalTimers = new Set(); // 定时器追踪
    this.requestCount = 0; // API请求计数
    this.maxRequestPerSecond = 10; // 每秒最大请求数
    this.lastRequestTime = Date.now();
    this.isMonitoring = false;
  }

  /**
   * 开始性能监控
   */
  startMonitoring() {
    if (this.isMonitoring) return;
    
    this.isMonitoring = true;
    
    // 内存监控 - 降低频率到每30秒一次
    this.memoryMonitor = setInterval(() => {
      this.checkMemoryUsage();
    }, 30000); // 从5秒改为30秒
    
    // 任务监控 - 大幅降低频率到每10秒一次
    this.taskMonitor = setInterval(() => {
      this.checkLongRunningTasks();
    }, 10000); // 从1秒改为10秒
    
    console.log('🔍 性能监控已启动 (降低频率版本)');
  }

  /**
   * 停止性能监控
   */
  stopMonitoring() {
    if (!this.isMonitoring) return;
    
    this.isMonitoring = false;
    
    if (this.memoryMonitor) {
      clearInterval(this.memoryMonitor);
    }
    
    if (this.taskMonitor) {
      clearInterval(this.taskMonitor);
    }
    
    console.log('⏹️ 性能监控已停止');
  }

  /**
   * 检查内存使用情况
   */
  checkMemoryUsage() {
    if (performance.memory) {
      const used = performance.memory.usedJSHeapSize;
      const total = performance.memory.totalJSHeapSize;
      const limit = performance.memory.jsHeapSizeLimit;
      
      const usagePercent = (used / limit) * 100;
      
      if (usagePercent > 80) {
        console.warn('⚠️ 内存使用过高:', {
          used: `${(used / 1024 / 1024).toFixed(2)}MB`,
          total: `${(total / 1024 / 1024).toFixed(2)}MB`,
          limit: `${(limit / 1024 / 1024).toFixed(2)}MB`,
          percentage: `${usagePercent.toFixed(2)}%`
        });
        
        // 触发垃圾回收建议
        if (window.gc && usagePercent > 90) {
          console.warn('🗑️ 尝试手动垃圾回收');
          window.gc();
        }
      }
    }
  }

  /**
   * 检查长时间运行的任务
   */
  checkLongRunningTasks() {
    // 检查是否有任务长时间占用主线程
    const start = performance.now();
    setTimeout(() => {
      const delay = performance.now() - start;
      // 大幅提高阈值，减少误报（从200ms提高到1000ms）
      if (delay > 1000) {
        console.warn('⏰ 检测到主线程阻塞:', `${delay.toFixed(2)}ms`);
        
        // 只有在非常严重阻塞时才报错（超过2000ms）
        if (delay > 2000) {
          console.error('🚨 严重主线程阻塞:', `${delay.toFixed(2)}ms`);
          
          // 如果阻塞超过5秒，可能需要清理资源
          if (delay > 5000) {
            console.error('💀 极严重主线程阻塞:', `${delay.toFixed(2)}ms`, '建议刷新页面');
            // 可以考虑自动清理一些资源
            this.clearAllTimers();
          }
        }
      }
    }, 0);
  }

  /**
   * 循环检测装饰器
   */
  detectCycle(functionName, maxCalls = 100, timeWindow = 1000) {
    return (target, propertyKey, descriptor) => {
      const originalMethod = descriptor.value;
      
      descriptor.value = function(...args) {
        const now = Date.now();
        const key = `${functionName}_${propertyKey}`;
        
        if (!this.cycleDetection.has(key)) {
          this.cycleDetection.set(key, { count: 0, timestamp: now });
        }
        
        const cycleInfo = this.cycleDetection.get(key);
        
        // 重置计数器如果时间窗口已过
        if (now - cycleInfo.timestamp > timeWindow) {
          cycleInfo.count = 0;
          cycleInfo.timestamp = now;
        }
        
        cycleInfo.count++;
        
        if (cycleInfo.count > maxCalls) {
          console.error('🔄 检测到可能的无限循环:', {
            function: propertyKey,
            calls: cycleInfo.count,
            timeWindow: `${timeWindow}ms`
          });
          return; // 阻止继续执行
        }
        
        return originalMethod.apply(this, args);
      };
      
      return descriptor;
    };
  }

  /**
   * API请求频率控制
   */
  throttleRequests() {
    const now = Date.now();
    const timeDiff = now - this.lastRequestTime;
    
    if (timeDiff < 1000) {
      this.requestCount++;
      
      // 大幅提高每秒请求数限制（从20提高到50）
      if (this.requestCount > 50) {
        console.warn('🚫 API请求频率过高, 已限制请求');
        return false;
      }
    } else {
      this.requestCount = 1;
      this.lastRequestTime = now;
    }
    
    return true;
  }

  /**
   * 定时器管理
   */
  trackTimer(timer) {
    this.intervalTimers.add(timer);
    return timer;
  }

  clearTimer(timer) {
    clearInterval(timer);
    clearTimeout(timer);
    this.intervalTimers.delete(timer);
  }

  clearAllTimers() {
    this.intervalTimers.forEach(timer => {
      clearInterval(timer);
      clearTimeout(timer);
    });
    this.intervalTimers.clear();
    console.log('🧹 已清理所有定时器');
  }

  /**
   * 组件性能分析
   */
  analyzeComponent(componentName) {
    return {
      beforeMount: () => {
        console.time(`${componentName} Mount Time`);
      },
      
      mounted: () => {
        console.timeEnd(`${componentName} Mount Time`);
      },
      
      beforeUnmount: () => {
        console.log(`🔄 ${componentName} 开始卸载`);
        this.clearAllTimers();
      },
      
      unmounted: () => {
        console.log(`✅ ${componentName} 卸载完成`);
      }
    };
  }
}

// 创建全局实例
const performanceMonitor = new PerformanceMonitor();

// 自动启动监控（仅在开发环境）
if (import.meta.env.DEV) {
  performanceMonitor.startMonitoring();
  
  // 页面卸载时清理
  window.addEventListener('beforeunload', () => {
    performanceMonitor.stopMonitoring();
  });
}

export default performanceMonitor; 