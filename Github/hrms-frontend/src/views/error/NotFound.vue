<!--
  404错误页面组件
  
  功能说明：
  1. 显示友好的404错误信息
  2. 提供返回首页和上一页的导航选项
  3. 响应式设计，适配各种设备
  4. 美观的视觉设计，减少用户挫败感
-->

<template>
  <div class="not-found-page">
    <!-- 错误内容容器 -->
    <div class="error-container">
      <!-- 404图标和数字 -->
      <div class="error-visual">
        <div class="error-number">404</div>
        <div class="error-icon">
          <el-icon size="120"><DocumentDelete /></el-icon>
        </div>
      </div>

      <!-- 错误信息 -->
      <div class="error-content">
        <h1 class="error-title">页面未找到</h1>
        <p class="error-description">
          抱歉，您访问的页面不存在或已被移除。
          <br>
          请检查URL是否正确，或者使用下面的按钮返回。
        </p>

        <!-- 操作按钮 -->
        <div class="error-actions">
          <el-button 
            type="primary" 
            size="large" 
            @click="goHome"
            class="action-button"
          >
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
          
          <el-button 
            size="large" 
            @click="goBack"
            class="action-button"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回上页
          </el-button>
        </div>

        <!-- 帮助信息 -->
        <div class="help-info">
          <p class="help-text">
            如果您认为这是一个错误，请联系系统管理员
          </p>
          <div class="help-links">
            <a href="mailto:admin@company.com" class="help-link">
              <el-icon><Message /></el-icon>
              联系管理员
            </a>
            <a href="tel:400-123-4567" class="help-link">
              <el-icon><Phone /></el-icon>
              技术支持
            </a>
          </div>
        </div>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>
  </div>
</template>

<script setup>
/**
 * 404错误页面脚本
 * 
 * 功能：
 * 1. 处理页面导航操作
 * 2. 根据用户角色智能跳转
 * 3. 提供友好的用户体验
 */

import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { 
  DocumentDelete, House, ArrowLeft, Message, Phone 
} from '@element-plus/icons-vue';

// 路由实例
const router = useRouter();

/**
 * 返回首页
 * 根据用户角色和认证状态智能跳转到合适的页面
 */
const goHome = () => {
  // 获取用户认证信息
  const isAuthenticated = !!localStorage.getItem('auth_token');
  const userRole = localStorage.getItem('user_role');
  
  // 根据认证状态和角色决定跳转目标
  if (!isAuthenticated) {
    // 未认证用户跳转到登录页
    router.push('/login');
    ElMessage.info('请先登录系统');
  } else if (userRole && userRole.toUpperCase() === 'ADMIN') {
    // 管理员跳转到管理员仪表板
    router.push('/admin/dashboard');
    ElMessage.success('已返回管理员首页');
  } else {
    // 员工跳转到员工仪表板
    router.push('/employee/dashboard');
    ElMessage.success('已返回员工首页');
  }
};

/**
 * 返回上一页
 * 使用浏览器历史记录返回，如果没有历史记录则跳转到首页
 */
const goBack = () => {
  // 检查是否有历史记录
  if (window.history.length > 1) {
    // 有历史记录，返回上一页
    router.go(-1);
    ElMessage.success('已返回上一页');
  } else {
    // 没有历史记录，跳转到首页
    goHome();
  }
};
</script>

<style lang="scss" scoped>
/* ==================== 页面容器样式 ==================== */

/**
 * 404页面主容器
 * 全屏布局，居中显示错误信息
 */
.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/**
 * 错误内容容器
 * 白色背景卡片，包含所有错误信息和操作
 */
.error-container {
  background: white;
  border-radius: 20px;
  padding: 60px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  text-align: center;
  max-width: 600px;
  width: 100%;
  position: relative;
  z-index: 10;
}

/* ==================== 错误视觉元素 ==================== */

/**
 * 错误视觉区域
 * 包含404数字和图标
 */
.error-visual {
  margin-bottom: 40px;
  position: relative;
}

/**
 * 404数字样式
 * 大号字体，渐变色彩
 */
.error-number {
  font-size: 8rem;
  font-weight: 900;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: transparent; /* 标准属性，用于兼容性 */
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

/**
 * 错误图标样式
 * 半透明效果，与数字形成层次
 */
.error-icon {
  color: #e0e6ed;
  opacity: 0.6;
}

/* ==================== 错误内容区域 ==================== */

/**
 * 错误标题样式
 */
.error-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 20px;
  line-height: 1.2;
}

/**
 * 错误描述文本样式
 */
.error-description {
  font-size: 1.1rem;
  color: #7f8c8d;
  line-height: 1.6;
  margin-bottom: 40px;
  max-width: 480px;
  margin-left: auto;
  margin-right: auto;
}

/* ==================== 操作按钮区域 ==================== */

/**
 * 操作按钮容器
 * 水平排列，响应式布局
 */
.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

/**
 * 操作按钮样式
 * 增大尺寸，添加图标间距
 */
.action-button {
  padding: 12px 24px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 12px;
  min-width: 140px;
  
  /* 按钮内图标和文字间距 */
  .el-icon {
    margin-right: 8px;
  }
  
  /* 悬停效果 */
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  }
}

/* ==================== 帮助信息区域 ==================== */

/**
 * 帮助信息容器
 */
.help-info {
  border-top: 1px solid #ecf0f1;
  padding-top: 30px;
}

/**
 * 帮助文本样式
 */
.help-text {
  font-size: 0.9rem;
  color: #95a5a6;
  margin-bottom: 20px;
}

/**
 * 帮助链接容器
 * 水平排列联系方式
 */
.help-links {
  display: flex;
  gap: 24px;
  justify-content: center;
  flex-wrap: wrap;
}

/**
 * 帮助链接样式
 * 图标和文字组合，悬停效果
 */
.help-link {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #3498db;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  
  &:hover {
    background-color: #f8f9fa;
    color: #2980b9;
    transform: translateY(-1px);
  }
  
  .el-icon {
    font-size: 1.1rem;
  }
}

/* ==================== 背景装饰 ==================== */

/**
 * 背景装饰容器
 * 绝对定位，不影响内容布局
 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

/**
 * 装饰圆圈基础样式
 * 半透明白色圆圈，动画效果
 */
.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

/**
 * 装饰圆圈具体样式
 * 不同大小和位置的圆圈
 */
.circle-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.circle-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

/**
 * 浮动动画
 * 圆圈上下浮动效果
 */
@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

/* ==================== 响应式设计 ==================== */

/**
 * 平板设备适配 (768px以下)
 */
@media (max-width: 768px) {
  .error-container {
    padding: 40px 30px;
    margin: 20px;
  }
  
  .error-number {
    font-size: 6rem;
  }
  
  .error-title {
    font-size: 2rem;
  }
  
  .error-description {
    font-size: 1rem;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .action-button {
    width: 100%;
    max-width: 200px;
  }
  
  .help-links {
    flex-direction: column;
    align-items: center;
  }
}

/**
 * 手机设备适配 (480px以下)
 */
@media (max-width: 480px) {
  .not-found-page {
    padding: 10px;
  }
  
  .error-container {
    padding: 30px 20px;
    border-radius: 15px;
  }
  
  .error-number {
    font-size: 4.5rem;
  }
  
  .error-title {
    font-size: 1.5rem;
  }
  
  .error-description {
    font-size: 0.9rem;
    margin-bottom: 30px;
  }
  
  .error-icon {
    .el-icon {
      font-size: 80px !important;
    }
  }
  
  .decoration-circle {
    display: none; /* 在小屏幕上隐藏装饰元素 */
  }
}

/**
 * 超小屏幕适配 (320px以下)
 */
@media (max-width: 320px) {
  .error-number {
    font-size: 3.5rem;
  }
  
  .error-title {
    font-size: 1.3rem;
  }
  
  .action-button {
    min-width: 120px;
    padding: 10px 16px;
    font-size: 0.9rem;
  }
}
</style> 