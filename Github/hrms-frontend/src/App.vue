<template>
  <div id="app">
    <!-- 顶部导航栏 -->
    <header v-if="showHeader" class="app-header">
      <div class="header-container">
        <!-- 左侧Logo和标题区域 -->
        <div class="header-left">
          <!-- 移除header内部的返回按钮 -->
          <div class="logo-section">
            <img src="@/assets/logo.svg" alt="HRMS" class="logo" />
            <span class="app-title">人力资源管理系统</span>
          </div>
        </div>
        
        <!-- 右侧用户信息和操作区域 -->
        <div class="header-right" v-if="authStore.isAuthenticated">
          <!-- 用户信息卡片 -->
          <div class="user-card">
            <el-avatar 
              :size="avatarSize" 
              class="user-avatar"
            >
              {{ displayUserName.charAt(0) }}
            </el-avatar>
            <div class="user-details">
              <span class="user-name">{{ displayUserName }}</span>
              <el-tag 
                :type="isAdmin ? 'danger' : 'success'" 
                size="small"
                class="user-role-tag"
              >
                {{ isAdmin ? '管理员' : '员工' }}
              </el-tag>
            </div>
          </div>
          
          <!-- 退出登录按钮 -->
          <el-button type="danger" size="small" @click="handleLogout" plain class="logout-button">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="app-main" :class="{ 'no-header': !showHeader }" :style="mainStyle">
      <!-- 页面级返回按钮 - 只在这里显示 -->
      <div v-if="showBackButton" class="page-back-button">
        <el-button 
          type="info" 
          size="small" 
          @click="handleGoBack"
          class="back-button"
          circle
        >
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
      </div>
      
      <router-view />
    </main>

    <!-- 底部信息栏 -->
    <footer v-if="showFooter" class="app-footer">
      <p>&copy; 2025 人力资源管理系统 - 版本 1.0.0</p>
    </footer>
  </div>
</template>

<script setup>
import { computed, watch, nextTick, ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, SwitchButton } from '@element-plus/icons-vue';
import { useAuthStore } from '@/store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 新增：获取导航栏实际高度
const headerHeight = ref(64); // 默认高度
const avatarSize = ref(36); // 默认头像大小

// 🔧 新增：实时用户信息响应
const realTimeUserName = ref('');
const realTimeUserRole = ref('');

// 更新实时用户信息
const updateRealTimeUserInfo = () => {
  const storedUserName = localStorage.getItem('user_name');
  const storedUserRole = localStorage.getItem('user_role');
  const storedUserInfo = localStorage.getItem('user_info');
  
  console.log('🔄 App.vue 更新用户信息:', {
    storedUserName,
    storedUserRole,
    authStoreUserName: authStore.userName
  });
  
  // 优先使用localStorage中的最新信息
  if (storedUserName && storedUserName !== realTimeUserName.value) {
    realTimeUserName.value = storedUserName;
    console.log('✅ App.vue 用户名已更新:', storedUserName);
  }
  
  if (storedUserRole) {
    realTimeUserRole.value = storedUserRole;
  }
  
  // 如果localStorage中的信息更新了，但store中的信息还是旧的，更新store
  if (storedUserName && storedUserName !== authStore.userName && storedUserInfo) {
    try {
      const userInfo = JSON.parse(storedUserInfo);
      console.log('🔄 同步store信息:', userInfo);
      
      // 更新store中的用户信息
      authStore.userName = storedUserName;
      authStore.userRole = storedUserRole || authStore.userRole;
    } catch (error) {
      console.warn('解析用户信息失败:', error);
    }
  }
};

// 计算属性：优先使用实时信息，回退到store信息
const displayUserName = computed(() => {
  const name = realTimeUserName.value || authStore.userName || '用户';
  console.log('🎯 App.vue 显示用户名:', name);
  return name;
});

const displayUserRole = computed(() => {
  return realTimeUserRole.value || authStore.userRole;
});

const isAdmin = computed(() => {
  return (displayUserRole.value && displayUserRole.value.toUpperCase() === 'ADMIN') || authStore.isAdmin;
});

// 防抖处理滚动位置重置
let scrollResetTimer = null;
const fixScrollPosition = () => {
  // 检查是否是Dashboard页面，如果是则跳过滚动重置
  if (route.name === 'EmployeeDashboard') {
    console.log('📍 Dashboard页面跳过滚动重置，保持滚动位置');
    return;
  }
  
  if (scrollResetTimer) {
    clearTimeout(scrollResetTimer);
  }
  scrollResetTimer = setTimeout(() => {
    try {
      nextTick(() => {
        if (document.body && document.documentElement) {
          window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
          document.body.scrollTop = 0;
          document.documentElement.scrollTop = 0;
        }
      });
    } catch (error) {
      console.warn('滚动重置失败:', error);
    }
  }, 50);
};

// 监听localStorage变化
const handleStorageChange = (e) => {
  if (e.key && e.key.startsWith('user_')) {
    console.log('📝 App.vue 检测到用户信息变化:', {
      key: e.key,
      oldValue: e.oldValue,
      newValue: e.newValue
    });
    
    // 延迟更新，确保所有相关字段都已更新
    setTimeout(() => {
      updateRealTimeUserInfo();
    }, 100);
  }
};

onMounted(() => {
  // 计算实际导航栏高度
  const header = document.querySelector('.app-header');
  if (header) {
    headerHeight.value = header.clientHeight;
  }
  
  // 检测高DPI环境
  detectHighDPI();
  
  // 初始滚动位置重置
  fixScrollPosition();
  
  // 🔧 初始化实时用户信息
  updateRealTimeUserInfo();
  
  // 🔧 监听localStorage变化
  window.addEventListener('storage', handleStorageChange);
  
  // 🔧 定期检查用户信息变化（用于同一页面内的变化）
  const userInfoCheckInterval = setInterval(() => {
    updateRealTimeUserInfo();
  }, 2000);
  
  // 保存定时器引用以便清理
  window.userInfoCheckInterval = userInfoCheckInterval;
});

// 检测高DPI环境并调整元素大小
const detectHighDPI = () => {
  try {
    const isHighDPI = window.matchMedia(
      "(-webkit-min-device-pixel-ratio: 1.5), (min-resolution: 144dpi), (min-resolution: 1.5dppx)"
    ).matches;
    
    if (isHighDPI) {
      avatarSize.value = 44; // 高DPI下增大头像尺寸
    }
  } catch (error) {
    console.warn('高DPI检测失败:', error);
  }
};

const showHeader = computed(() => route.meta.showHeader !== false);
const showFooter = computed(() => route.meta.showFooter !== false);

const showBackButton = computed(() => {
  const dashboardPaths = ['/employee/dashboard', '/admin/dashboard'];
  return authStore.isAuthenticated && !dashboardPaths.includes(route.path);
});

// 动态计算主内容区域padding-top
const mainStyle = computed(() => {
  return showHeader.value 
    ? { 'padding-top': `${headerHeight.value}px` } 
    : {};
});

const handleGoBack = () => {
  if (window.history.length > 1) {
    router.go(-1);
  } else {
    const homePath = isAdmin.value ? '/admin/dashboard' : '/employee/dashboard';
    router.push(homePath);
  }
};

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    authStore.logout();
    ElMessage.success('已退出登录');
  } catch {
    // User cancelled
  }
};

// 优化路由监听，添加防抖和条件检查
let routeWatchTimer = null;
watch(
  () => route.path,
  (newPath, oldPath) => {
    // 避免相同路径重复执行
    if (newPath === oldPath) return;
    
    // 特殊处理：如果是导航到Dashboard页面，跳过滚动重置
    // 特别是从公告页面返回Dashboard时，需要保持滚动位置
    if (newPath === '/employee/dashboard') {
      console.log('📍 导航到Dashboard页面，跳过滚动重置');
      // 如果是从公告页面返回，滚动位置已经在beforeEach中设置了
      if (oldPath === '/employee/announcements') {
        console.log('📍 从公告页面返回Dashboard，保持滚动位置');
      }
      return;
    }
    
    // 清除之前的定时器
    if (routeWatchTimer) {
      clearTimeout(routeWatchTimer);
    }
    
    // 防抖处理
    routeWatchTimer = setTimeout(() => {
      fixScrollPosition();
    }, 100);
  }
);

// 组件卸载时清理定时器
onUnmounted(() => {
  if (scrollResetTimer) {
    clearTimeout(scrollResetTimer);
  }
  if (routeWatchTimer) {
    clearTimeout(routeWatchTimer);
  }
  
  // 🔧 清理用户信息监听
  window.removeEventListener('storage', handleStorageChange);
  
  if (window.userInfoCheckInterval) {
    clearInterval(window.userInfoCheckInterval);
  }
});
</script>

<style lang="scss">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background-color: #f8f9fa;
  min-height: 100vh;
}

// 全局隐藏可能在彩色标题栏内的返回按钮
.page-header, .header-content, .action-section, .header-actions,
.el-tabs__header, .el-card__header, .table-header, .card-header,
.salary-tabs .el-tabs__header, .overview-card, .stat-card {
  // 简化选择器，避免使用性能密集型的:has()和:contains()
  .back-btn,
  .return-btn,
  .go-back,
  .btn-back,
  .el-button--back {
    display: none !important;
    visibility: hidden !important;
    opacity: 0 !important;
    pointer-events: none !important;
  }
}

// 特别针对Element Plus组件的返回按钮
.el-button {
  &[data-action="back"],
  &[data-role="back"] {
    display: none !important;
    visibility: hidden !important;
  }
}

// 确保我们自己的返回按钮始终可见
.back-button-container .back-button {
  display: flex !important;
  visibility: visible !important;
  opacity: 1 !important;
  pointer-events: auto !important;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
}

.router-link-active {
  color: #409EFF;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* ==================== 全局样式重置 ==================== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f5f7fa;
  overflow-x: hidden;
  /* 移除全局smooth滚动，避免与Dashboard滚动位置保持功能冲突 */
  scroll-behavior: auto;
  position: relative;
  -webkit-text-size-adjust: 100%;
  -ms-text-size-adjust: 100%;
  text-size-adjust: 100%;
}

/* 为非Dashboard页面添加smooth滚动 */
body:not(.dashboard-page) {
  scroll-behavior: smooth;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: visible;
  width: 100%;
  max-width: 100vw;
}

/* ==================== 顶部导航栏样式 ==================== */
.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  width: 100%;
  flex-shrink: 0;
}

.header-container {
  width: 100%;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 64px;
  box-sizing: border-box;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1; /* 占据左侧空间 */
  min-width: 0; /* 允许缩小 */
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden; /* 防止文字溢出 */
}

.logo {
  height: 40px;
  width: 40px;
  flex-shrink: 0; /* 防止logo被压缩 */
}

.app-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: white;
  white-space: nowrap; /* 防止文字换行 */
  overflow: hidden;
  text-overflow: ellipsis; /* 文字过长时显示省略号 */
  max-width: 200px; /* 限制最大宽度 */
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0; /* 防止右侧内容被压缩 */
  margin-left: 16px; /* 与左侧保持距离 */
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.1);
  padding: 6px 16px; /* 调整内边距 */
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-width: 0; /* 允许缩小 */
}

.user-avatar {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: 600;
  flex-shrink: 0; /* 防止头像被压缩 */
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0; /* 允许文字区域缩小 */
}

.user-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: white;
  line-height: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis; /* 姓名过长时显示省略号 */
  max-width: 100px; /* 限制姓名最大宽度 */
}

.user-role-tag {
  font-size: 0.7rem;
  height: 18px;
  line-height: 16px;
  white-space: nowrap;
}

.logout-button {
  padding: 8px 12px; /* 调整按钮内边距 */
  flex-shrink: 0; /* 防止按钮被压缩 */
}

.back-button {
  color: white;
  border-color: rgba(255, 255, 255, 0.5);
  padding: 6px 10px; /* 调整按钮内边距 */
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.8);
    color: white;
  }
}

/* ==================== 主要内容区域样式 ==================== */
.app-main {
  flex: 1;
  min-height: calc(100vh - 64px);
  background-color: #f5f7fa;
  transition: padding-top 0.3s ease;
  padding: 0;
  width: 100%;
  overflow-x: hidden;
  
  &.no-header {
    padding-top: 0;
    min-height: 100vh;
  }
}

/* 页面级返回按钮样式 */
.page-back-button {
  position: fixed;
  top: 80px; /* 在header下方 */
  left: 24px;
  z-index: 999;
  
  .back-button {
    background: white;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    &:hover {
      background: #f5f7fa;
      border-color: #409eff;
      color: #409eff;
    }
  }
}

/* 移除所有可能的重复返回按钮 */
.el-page-header .el-page-header__back,
.page-header .back-btn,
.header-content .back-btn,
.action-section .back-btn,
.header-actions .back-btn,
.el-tabs__header .back-btn,
.el-card__header .back-btn,
.table-header .back-btn,
.card-header .back-btn,
.salary-tabs .el-tabs__header .back-btn,
.overview-card .back-btn,
.stat-card .back-btn {
  display: none !important;
}

/* 隐藏可能在各种容器中的返回按钮 */
.el-button[data-back="true"],
.el-button[role="back"],
.el-button.return-button,
.el-button.go-back-btn {
  display: none !important;
}

/* ==================== 底部信息栏样式 ==================== */
.app-footer {
  background: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 20px 0;
  text-align: center;
  color: #909399;
  font-size: 0.875rem;
}

/* ==================== 高DPI显示器适配 ==================== */
@media screen and (-webkit-min-device-pixel-ratio: 1.5),
       screen and (min-resolution: 144dpi),
       screen and (min-resolution: 1.5dppx) {
  .app-header {
    transform: translateZ(0);
    -webkit-transform: translateZ(0);
    will-change: transform;
    z-index: 9999;
  }
  
  html, body {
    overflow: visible;
    position: static;
    height: auto;
    min-height: 100%;
  }
  
  #app {
    min-height: 100%;
    overflow: visible;
  }
  
  /* 高DPI下调整导航栏高度 */
  .header-container {
    height: 72px !important;
    min-height: 72px !important;
  }
  
  /* 高DPI下调整元素大小 */
  .logo {
    height: 44px;
    width: 44px;
  }
  
  .app-title {
    font-size: 1.3rem;
    max-width: 220px;
  }
  
  .user-name {
    font-size: 1rem;
    max-width: 120px;
  }
  
  .logout-button {
    padding: 9px 14px;
    font-size: 0.9rem;
  }
  
  .back-button {
    padding: 7px 12px;
    font-size: 0.9rem;
  }
}

/* 特定分辨率适配 (2560x1600 @ 150%缩放) */
@media screen and (min-width: 2560px) and (min-height: 1600px) {
  .app-main {
    padding-top: 0;
  }
  
  .header-container {
    min-height: 80px !important;
    height: auto !important;
    padding: 0 32px;
  }
  
  #app {
    max-height: none;
    overflow-y: visible;
  }
  
  /* 大屏幕下调整元素大小 */
  .logo {
    height: 48px;
    width: 48px;
  }
  
  .app-title {
    font-size: 1.4rem;
    max-width: 240px;
  }
  
  .user-card {
    padding: 8px 20px;
  }
  
  .user-name {
    font-size: 1.1rem;
    max-width: 140px;
  }
  
  .user-role-tag {
    font-size: 0.8rem;
    padding: 0 8px;
    height: 22px;
    line-height: 20px;
  }
  
  .logout-button {
    padding: 10px 16px;
    font-size: 1rem;
  }
  
  .back-button {
    padding: 8px 14px;
    font-size: 1rem;
  }
}

/* 响应式设计 - 解决小屏幕显示问题 */
@media (max-width: 1200px) {
  .app-title {
    max-width: 180px;
  }
  
  .user-name {
    max-width: 80px;
  }
}

@media (max-width: 992px) {
  .app-title {
    max-width: 160px;
  }
  
  .user-name {
    max-width: 70px;
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }
  
  .app-title {
    display: none; /* 在小屏幕隐藏标题 */
  }
  
  .user-card {
    padding: 6px 12px;
  }
  
  .user-details {
    display: none; /* 在小屏幕隐藏用户详情 */
  }
  
  .logout-button span {
    display: none; /* 在小屏幕隐藏"退出登录"文字 */
  }
}

@media (max-width: 480px) {
  .back-button span {
    display: none; /* 在超小屏幕隐藏"返回"文字 */
  }
  
  .logo {
    height: 36px;
    width: 36px;
  }
}

/* ==================== 全局返回按钮统一样式 ==================== */
.back-button-container {
  position: fixed !important;
  top: 80px !important;
  left: 24px !important;
  z-index: 9999 !important;
  pointer-events: auto !important;
  visibility: visible !important;
  opacity: 1 !important;
}

.back-button {
  width: 48px !important;
  height: 48px !important;
  min-width: 48px !important;
  min-height: 48px !important;
  border-radius: 50% !important;
  padding: 0 !important;
  margin: 0 !important;
  background-color: rgba(255, 255, 255, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.8) !important;
  color: #495057 !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  backdrop-filter: blur(10px) !important;
  -webkit-backdrop-filter: blur(10px) !important;
  transition: all 0.3s ease !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  cursor: pointer !important;
  user-select: none !important;
  
  /* 覆盖Element Plus的默认样式 */
  span {
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    margin: 0 !important;
    padding: 0 !important;
  }
  
  .el-icon {
    font-size: 20px !important;
    margin: 0 !important;
    padding: 0 !important;
    line-height: 1 !important;
  }
  
  &:hover {
    background-color: rgba(255, 255, 255, 1) !important;
    border-color: rgba(255, 255, 255, 1) !important;
    color: #212529 !important;
    transform: translateY(-2px) !important;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2) !important;
  }
  
  &:active {
    transform: translateY(-1px) !important;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  }
  
  &:focus {
    outline: none !important;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 0 2px rgba(64, 158, 255, 0.3) !important;
  }
  
  /* 确保按钮始终可见 */
  &.el-button {
    visibility: visible !important;
    opacity: 1 !important;
    pointer-events: auto !important;
  }
}

/* 强制覆盖所有可能的按钮样式冲突 */
div.back-button-container .back-button.el-button {
  visibility: visible !important;
  opacity: 1 !important;
  display: flex !important;
  pointer-events: auto !important;
  width: 48px !important;
  height: 48px !important;
  border-radius: 50% !important;
}

/* 覆盖页面特定的隐藏样式 */
.employee-salary .back-button-container,
.employee-profile .back-button-container,
.employee-leave .back-button-container,

.employee-announcements .back-button-container {
  position: fixed !important;
  visibility: visible !important;
  opacity: 1 !important;
  pointer-events: auto !important;
  z-index: 9999 !important;
}

.employee-salary .back-button,
.employee-profile .back-button,
.employee-leave .back-button,

.employee-announcements .back-button {
  visibility: visible !important;
  opacity: 1 !important;
  display: flex !important;
  pointer-events: auto !important;
}
</style>