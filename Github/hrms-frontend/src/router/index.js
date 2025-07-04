/**
 * Vue Router 路由配置文件
 * 
 * 功能说明：
 * 1. 配置应用程序的所有路由规则
 * 2. 实现基于角色的访问控制（RBAC）
 * 3. 处理认证状态检查和页面重定向
 * 4. 设置页面标题和元数据
 * 
 * 路由结构：
 * - 认证相关路由：登录、注册页面
 * - 员工端路由：员工功能模块
 * - 管理员端路由：管理员功能模块
 * - 错误处理路由：404页面等
 */

import { createRouter, createWebHistory } from 'vue-router';

/**
 * 路由配置数组
 * 每个路由对象包含：
 * - path: 路由路径
 * - name: 路由名称（用于编程式导航）
 * - component: 对应的Vue组件（懒加载）
 * - meta: 路由元信息（权限控制、页面标题等）
 * - children: 子路由（嵌套路由）
 * - redirect: 重定向规则
 */
const routes = [
  // ==================== 认证相关路由 ====================
  
  /**
   * 员工登录页面
   * 路径: /login
   * 权限: 无需认证
   * 特点: 隐藏头部和底部导航
   */
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { 
      title: '员工登录',
      requiresAuth: false, // 不需要登录认证
      showHeader: false,   // 不显示页面头部
      showFooter: false    // 不显示页面底部
    }
  },
  
  /**
   * 管理员登录页面
   * 路径: /admin-login
   * 权限: 无需认证
   * 特点: 与员工登录分离，独立的管理员入口
   */
  {
    path: '/admin-login',
    name: 'AdminLogin',
    component: () => import('@/views/auth/AdminLogin.vue'),
    meta: { 
      title: '管理员登录',
      requiresAuth: false,
      showHeader: false,
      showFooter: false
    }
  },
  


  // ==================== 员工端路由 ====================
  
  /**
   * 员工端主路由
   * 路径: /employee
   * 权限: 需要员工角色认证
   * 默认重定向到员工仪表板
   */
  {
    path: '/employee',
    name: 'Employee',
    redirect: '/employee/dashboard', // 默认跳转到仪表板
    meta: { requiresAuth: true, role: 'employee' },
    children: [
      /**
       * 员工仪表板
       * 路径: /employee/dashboard
       * 功能: 显示个人工作概览、快捷操作、统计信息
       */
      {
        path: 'dashboard',
        name: 'EmployeeDashboard',
        component: () => import('@/views/employee/Dashboard.vue'),
        meta: { 
          title: '员工首页',
          requiresAuth: true,
          role: 'employee'
        }
      },
      
      /**
       * 个人信息管理
       * 路径: /employee/profile
       * 功能: 查看和编辑个人资料、工作信息
       */
      {
        path: 'profile',
        name: 'EmployeeProfile',
        component: () => import('@/views/employee/Profile.vue'),
        meta: { 
          title: '个人信息',
          requiresAuth: true,
          role: 'employee'
        }
      },
      
      /**
       * 薪资查看
       * 路径: /employee/salary
       * 功能: 查看薪资详情、历史记录、工资条
       */
      {
        path: 'salary',
        name: 'EmployeeSalary',
        component: () => import('@/views/employee/Salary.vue'),
        meta: { 
          title: '薪资查看',
          requiresAuth: true,
          role: 'employee'
        }
      },
      
      /**
       * 公告通知
       * 路径: /employee/announcements
       * 功能: 查看公司公告、通知消息
       */
      {
        path: 'announcements',
        name: 'EmployeeAnnouncements',
        component: () => import('@/views/employee/Announcements.vue'),
        meta: { 
          title: '公告通知',
          requiresAuth: true,
          role: 'employee'
        }
      },
      

      
      /**
       * 请假申请
       * 路径: /employee/leave
       * 功能: 提交请假申请、查看申请状态和历史记录
       */
      {
        path: 'leave',
        name: 'EmployeeLeave',
        component: () => import('@/views/employee/Leave.vue'),
        meta: { 
          title: '请假申请',
          requiresAuth: true,
          role: 'employee'
        }
      }
    ]
  },

  // ==================== 管理员端路由 ====================
  
  /**
   * 管理员端主路由
   * 路径: /admin
   * 权限: 需要管理员角色认证
   * 默认重定向到管理员仪表板
   */
  {
    path: '/admin',
    name: 'Admin',
    redirect: '/admin/dashboard', // 默认跳转到管理仪表板
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      /**
       * 管理员仪表板
       * 路径: /admin/dashboard
       * 功能: 显示系统概览、统计数据、图表分析
       */
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { 
          title: '管理员首页',
          requiresAuth: true,
          role: 'admin'
        }
      },
      
      /**
       * 员工管理
       * 路径: /admin/employees
       * 功能: 员工信息管理、入职离职处理
       */
      {
        path: 'employees',
        name: 'AdminEmployees',
        component: () => import('@/views/admin/Employees.vue'),
        meta: { 
          title: '员工管理',
          requiresAuth: true,
          role: 'admin'
        }
      },
      
      /**
       * 公告管理
       * 路径: /admin/announcements
       * 功能: 发布、编辑、删除公司公告
       */
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/Announcements.vue'),
        meta: { 
          title: '公告管理',
          requiresAuth: true,
          role: 'admin'
        }
      },
      
      /**
       * 数据报表
       * 路径: /admin/reports
       * 功能: 各类统计报表、数据分析
       */
      {
        path: 'reports',
        name: 'AdminReports',
        component: () => import('@/views/admin/Reports.vue'),
        meta: { 
          title: '数据报表',
          requiresAuth: true,
          role: 'admin'
        }
      },
      
      /**
       * 请假审批
       * 路径: /admin/leave-approval
       * 功能: 查看和审批员工请假申请
       */
      {
        path: 'leave-approval',
        name: 'AdminLeaveApproval',
        component: () => import('@/views/admin/LeaveApproval.vue'),
        meta: { 
          title: '请假审批',
          requiresAuth: true,
          role: 'admin'
        }
      },
      
      /**
       * 薪资管理
       * 路径: /admin/salary
       * 功能: 薪资设置、发放管理、薪资标准
       */
      {
        path: 'salary',
        name: 'AdminSalary',
        component: () => import('@/views/admin/Salary.vue'),
        meta: { 
          title: '薪资管理',
          requiresAuth: true,
          role: 'admin'
        }
      },
      
      /**
       * 管理员设置
       * 路径: /admin/settings
       * 功能: 个人信息管理、密码修改、系统设置
       */
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/AdminSettings.vue'),
        meta: { 
          title: '个人设置',
          requiresAuth: true,
          role: 'admin'
        }
      }
    ]
  },

  // ==================== 特殊路由 ====================
  
  /**
   * 根路径重定向
   * 路径: /
   * 功能: 根据用户认证状态和角色智能重定向
   * 逻辑:
   * - 未登录 -> 跳转到登录页
   * - 管理员 -> 跳转到管理员仪表板
   * - 员工 -> 跳转到员工仪表板
   */
  {
    path: '/',
    redirect: (to) => {
      // 从本地存储获取用户角色和认证状态
      const userRole = localStorage.getItem('user_role');
      const isAuthenticated = !!localStorage.getItem('auth_token');
      
      // 未认证用户跳转到登录页
      if (!isAuthenticated) {
        return '/login';
      }
      
      // 根据角色跳转到对应的仪表板
      return userRole && userRole.toUpperCase() === 'ADMIN' ? '/admin/dashboard' : '/employee/dashboard';
    }
  },

  /**
   * 404错误页面
   * 路径: 匹配所有未定义的路由
   * 功能: 显示页面未找到错误信息
   */
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { 
      title: '页面未找到',
      requiresAuth: false
    }
  }
];

/**
 * 创建路由实例
 * 使用HTML5 History模式，支持干净的URL
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  
  /**
   * 滚动行为配置
   * 功能：保持页面滚动位置，提升用户体验
   * 
   * @param {Object} to - 即将进入的目标路由对象
   * @param {Object} from - 当前导航正要离开的路由对象  
   * @param {Object} savedPosition - 浏览器前进/后退时保存的滚动位置
   * @returns {Object|Promise} 滚动位置配置
   */
  scrollBehavior(to, from, savedPosition) {
    console.log('🔄 ScrollBehavior 触发:', { 
      to: to.name, 
      from: from.name, 
      savedPosition
    });
    
    // 如果是浏览器的前进/后退操作，恢复到之前保存的位置
    if (savedPosition) {
      console.log('🔄 使用浏览器保存的位置:', savedPosition);
      return savedPosition;
    }
    
    // 如果目标路由有锚点，滚动到对应元素
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth'
      };
    }
    
    // Dashboard页面不自动滚动到顶部，保持当前位置
    // 特殊情况：从公告页面返回Dashboard时，位置已经在beforeEach中设置了
    if (to.name === 'EmployeeDashboard') {
      console.log('📍 Dashboard页面保持当前滚动位置');
      return false;
    }
    
    // 其他页面默认滚动到顶部
    return { left: 0, top: 0 };
  }
});

/**
 * 全局前置路由守卫
 * 
 * 功能说明：
 * 1. 身份认证检查 - 验证用户是否已登录
 * 2. 权限控制 - 检查用户角色是否匹配路由要求
 * 3. 页面标题设置 - 根据路由元信息设置浏览器标题
 * 4. 智能重定向 - 根据用户状态和角色进行页面跳转
 * 
 * 参数说明：
 * @param {Object} to - 即将进入的目标路由对象
 * @param {Object} from - 当前导航正要离开的路由对象
 * @param {Function} next - 路由跳转控制函数
 */

// 防止无限重定向的追踪变量
let redirectCount = 0;
const MAX_REDIRECT_COUNT = 5;

router.beforeEach((to, from, next) => {
  // 重置重定向计数器（每次新的导航周期）
  if (from.name === null) {
    redirectCount = 0;
  }
  
  // 特殊处理：从公告页面返回Dashboard时，预先设置滚动位置
  if (to.name === 'EmployeeDashboard' && from.name === 'EmployeeAnnouncements') {
    const savedScrollPosition = sessionStorage.getItem('dashboard_scroll_position');
    if (savedScrollPosition) {
      try {
        const position = JSON.parse(savedScrollPosition);
        console.log('🔄 BeforeEach预设滚动位置:', position);
        
        // 使用多种方式确保滚动位置被正确设置
        const setScrollPosition = () => {
          document.documentElement.scrollTop = position.y || 0;
          document.body.scrollTop = position.y || 0;
          window.scrollTo({
            top: position.y || 0,
            left: position.x || 0,
            behavior: 'auto'
          });
        };
        
        // 立即设置一次
        setScrollPosition();
        
        // 在下一个事件循环中再设置一次，确保DOM更新后生效
        setTimeout(setScrollPosition, 0);
        
        // 清除保存的位置
        sessionStorage.removeItem('dashboard_scroll_position');
      } catch (error) {
        console.warn('BeforeEach设置滚动位置失败:', error);
      }
    }
  }
  
  // 检查重定向次数，防止无限循环
  if (redirectCount > MAX_REDIRECT_COUNT) {
    console.error('检测到路由无限重定向，强制跳转到登录页');
    // 清除可能有问题的认证信息
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_role');
    localStorage.removeItem('user_name');
    localStorage.removeItem('user_info');
    redirectCount = 0;
    next('/login');
    return;
  }

  // 从本地存储获取用户认证信息
  const isAuthenticated = !!localStorage.getItem('auth_token'); // 是否已认证
  const userRole = localStorage.getItem('user_role'); // 用户角色
  
  // ==================== 页面标题设置 ====================
  // 根据路由元信息设置浏览器标题，格式：页面标题 | HRMS
  try {
    document.title = to.meta.title ? `${to.meta.title} | HRMS` : 'HRMS';
  } catch (error) {
    console.warn('设置页面标题失败:', error);
  }
  
  // ==================== 特殊路径处理 ====================
  // 对于静态资源和API请求，直接放行
  if (to.path.startsWith('/api/') || to.path.startsWith('/static/')) {
    next();
    return;
  }

  // ==================== 身份认证检查 ====================
  // 如果目标路由需要认证但用户未登录，重定向到登录页
  if (to.meta.requiresAuth && !isAuthenticated) {
    // 避免在已经是登录页时重复重定向
    if (to.path !== '/login') {
      redirectCount++;
      next('/login');
    } else {
      next();
    }
    return;
  }
  
  // ==================== 角色权限检查 ====================
  // 如果目标路由有角色要求且用户角色不匹配
  if (to.meta.role && to.meta.role !== userRole && isAuthenticated) {
    redirectCount++;
    
    // 根据用户实际角色重定向到对应的首页
    if (userRole && userRole.toUpperCase() === 'ADMIN' && to.path !== '/admin/dashboard') {
      next('/admin/dashboard');
          } else if (userRole && userRole.toUpperCase() === 'EMPLOYEE' && to.path !== '/employee/dashboard') {
      next('/employee/dashboard');
    } else if (!userRole) {
      // 角色信息异常，清除认证信息并重定向到登录页
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user_role');
      localStorage.removeItem('user_name');
      localStorage.removeItem('user_info');
      next('/login');
    } else {
      // 如果已经在目标页面，直接放行
      next();
    }
    return;
  }
  
  // ==================== 登录页面访问控制 ====================
  // 已登录用户访问登录相关页面时，重定向到对应的首页
  if (isAuthenticated && ['/login', '/admin-login', '/register'].includes(to.path)) {
    redirectCount++;
    
    if (userRole && userRole.toUpperCase() === 'ADMIN') {
      next('/admin/dashboard');
          } else if (userRole && userRole.toUpperCase() === 'EMPLOYEE') {
      next('/employee/dashboard');
    } else {
      // 角色信息异常，清除认证信息
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user_role');
      localStorage.removeItem('user_name');
      localStorage.removeItem('user_info');
      next('/login');
    }
    return;
  }
  
  // ==================== 正常路由跳转 ====================
  // 所有检查通过，允许正常访问目标路由
  redirectCount = 0; // 重置计数器
  next();
});

// 添加路由错误处理
router.onError((error) => {
  console.error('路由错误:', error);
  
  // 尝试恢复到安全页面
  const userRole = localStorage.getItem('user_role');
  const isAuthenticated = !!localStorage.getItem('auth_token');
  
  if (isAuthenticated && userRole && userRole.toUpperCase() === 'ADMIN') {
    router.replace('/admin/dashboard');
      } else if (isAuthenticated && userRole && userRole.toUpperCase() === 'EMPLOYEE') {
    router.replace('/employee/dashboard');
  } else {
    router.replace('/login');
  }
});

// 导出路由实例供应用程序使用
export default router;