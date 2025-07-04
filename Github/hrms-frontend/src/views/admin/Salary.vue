<template>
  <div class="admin-salary">
    <!-- 返回按钮 -->
    <div class="back-button-container">
      <el-button 
        @click="goBack" 
        class="back-button"
        :icon="ArrowLeft"
        circle
        size="large"
      />
    </div>
    
    <!-- 页面头部 -->
    <div class="page-header">
      <h1>薪资管理</h1>
      <p>管理员工薪资、设置薪资标准、发放工资</p>
    </div>

    <!-- 功能标签页 -->
    <el-tabs v-model="activeTab" class="salary-tabs">
      <!-- 薪资记录管理 -->
      <el-tab-pane label="薪资记录" name="records">
        <SalaryRecords />
      </el-tab-pane>
      
      <!-- 薪资统计报表 -->
      <el-tab-pane label="统计报表" name="statistics">
        <SalaryStatistics />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';
import SalaryRecords from './components/SalaryRecords.vue';
import SalaryStatistics from './components/SalaryStatistics.vue';

const router = useRouter();
const activeTab = ref('records');

// 返回上一页
const goBack = () => {
  // 清除当前路由历史记录，强制跳转到管理员仪表板
  window.history.replaceState(null, '', '/admin/dashboard');
  router.replace('/admin/dashboard').then(() => {
    // 确保页面完全刷新
    window.location.reload();
  }).catch(() => {
    // 如果路由跳转失败，直接使用浏览器跳转
    window.location.href = '/admin/dashboard';
  });
};
</script>

<style lang="scss" scoped>
.admin-salary {
  padding: 24px;
  position: relative;
  
  // 全局隐藏可能在彩色标题栏内的返回按钮
  .page-header, .header-content, .action-section, .header-actions,
  .el-tabs, .el-card, .table-header, .card-header {
    .el-button[aria-label*="返回"],
    .el-button:has(.el-icon svg[data-icon="ArrowLeft"]),
    .el-button:has(.el-icon[class*="arrow-left"]),
    .el-button:has(.el-icon.el-icon--arrow-left),
    .el-button .el-icon--arrow-left,
    .el-icon.el-icon--arrow-left,
    .el-button:contains("返回"),
    .back-btn,
    .return-btn {
      display: none !important;
      visibility: hidden !important;
    }
  }
  
  .back-button-container {
    position: absolute;
    top: 24px;
    left: 24px;
    z-index: 15;
    
    .back-button {
      background-color: rgba(255, 255, 255, 0.95);
      border: 1px solid rgba(255, 255, 255, 0.8);
      color: #495057;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      backdrop-filter: blur(10px);
      transition: all 0.3s ease;
      
      &:hover {
        background-color: rgba(255, 255, 255, 1);
        border-color: rgba(255, 255, 255, 1);
        color: #212529;
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
      }
      
      &:active {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }
    }
  }
  
  .page-header {
    margin-bottom: 24px;
    padding-left: 60px; // 为返回按钮留出空间
    
    h1 {
      font-size: 1.8rem;
      font-weight: 600;
      color: #2c3e50;
      margin-bottom: 8px;
    }
    
    p {
      color: #7f8c8d;
      margin: 0;
    }
  }
  
  .salary-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 24px;
    }
    
    :deep(.el-tabs__item) {
      font-size: 1rem;
      font-weight: 500;
    }
  }
}
</style> 