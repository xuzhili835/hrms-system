<template>
  <div class="employee-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-card">
        <div class="welcome-content">
          <div class="welcome-text">
            <h1>欢迎回来，{{ userInfo.name }}！</h1>
            <p>今天是 {{ currentDate }}，祝您工作愉快</p>
          </div>
          <div class="welcome-avatar">
            <el-avatar :size="80" :src="userInfo.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷功能区域 -->
    <div class="quick-actions">
      <h2 class="section-title">快捷功能</h2>
      <div class="actions-grid">
        <div class="action-card" @click="goToProfile">
          <div class="action-icon profile">
            <el-icon><User /></el-icon>
          </div>
          <h3>个人信息</h3>
          <p>查看和编辑个人资料</p>
        </div>
        
        <div class="action-card" @click="goToSalary">
          <div class="action-icon salary">
            <el-icon><Money /></el-icon>
          </div>
          <h3>薪资查看</h3>
          <p>查看工资单和薪资详情</p>
        </div>
        
        <div class="action-card" @click="goToAnnouncements">
          <div class="action-icon announcement">
            <el-icon><Bell /></el-icon>
          </div>
          <h3>公告通知</h3>
          <p>查看公司最新公告</p>
        </div>
        
        <div class="action-card" @click="goToLeave">
          <div class="action-icon leave">
            <el-icon><Calendar /></el-icon>
          </div>
          <h3>请假申请</h3>
          <p>提交请假申请和查看状态</p>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 - 水平布局 -->
    <div class="main-content">
      <!-- 左侧统计区域 -->
      <div class="left-section">
        <!-- 个人统计 -->
        <div class="personal-stats">
          <h2 class="section-title">个人统计</h2>
          <div class="stats-grid" v-loading="statsLoading">
            <el-card class="stat-card leave-stat">
              <div class="stat-content">
                <div class="stat-icon leave">
                  <el-icon><Calendar /></el-icon>
                </div>
                <div class="stat-info">
                  <h3>本月请假</h3>
                  <p class="stat-value">{{ personalStats.monthlyLeave }}天</p>
                  <span class="stat-desc">{{ personalStats.currentMonth }}</span>
                </div>
              </div>
            </el-card>
            
            <el-card class="stat-card work-stat">
              <div class="stat-content">
                <div class="stat-icon work">
                  <el-icon><Trophy /></el-icon>
                </div>
                <div class="stat-info">
                  <h3>工作年限</h3>
                  <p class="stat-value">{{ personalStats.workYears }}年</p>
                  <span class="stat-desc">入职时间 {{ personalStats.joinDate }}</span>
                </div>
              </div>
            </el-card>
            
            <el-card class="stat-card performance-stat">
              <div class="stat-content">
                <div class="stat-icon performance">
                  <el-icon><Money /></el-icon>
                </div>
                <div class="stat-info">
                  <h3>{{ personalStats.salaryTitle || '薪资信息' }}</h3>
                  <p class="stat-value">¥{{ personalStats.currentSalary }}</p>
                  <span class="stat-desc">{{ personalStats.salaryMonth }}</span>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </div>

      <!-- 右侧公告区域 -->
      <div class="right-section">
        <!-- 最新公告 -->
        <div class="latest-announcements">
          <h2 class="section-title">最新公告</h2>
          <el-card class="announcements-card">
            <div class="announcement-list">
              <div 
                v-for="announcement in announcements" 
                :key="announcement.id"
                class="announcement-item"
                @click="viewAnnouncement(announcement)"
              >
                <div class="announcement-content">
                  <h4>{{ announcement.title }}</h4>
                  <p>{{ announcement.content.substring(0, 60) }}{{ announcement.content.length > 60 ? '...' : '' }}</p>
                  <div class="announcement-meta">
                    <span class="date">{{ announcement.pubDate }}</span>
                    <el-tag type="info" size="small">{{ announcement.status }}</el-tag>
                  </div>
                </div>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              </div>
              
              <div v-if="announcements.length === 0" class="no-announcements">
                <el-empty description="暂无公告"></el-empty>
              </div>
            </div>
            
            <div class="view-all">
              <el-button type="primary" link @click="goToAnnouncements">
                查看全部公告
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getAnnouncements } from '@/api/announcement';
import { getEmployeeById } from '@/api/employee';
import { getMyLeaveApplications } from '@/api/leave';
import { getEmployeeSalaryHistory } from '@/api/salary';
import { 
  User, Money, Bell, Clock, Calendar, Trophy, Star, ArrowRight
} from '@element-plus/icons-vue';

const router = useRouter();

// 滚动位置保存相关
let scrollSaveTimer = null;

/**
 * 保存当前滚动位置到sessionStorage
 */
const saveScrollPosition = () => {
  const scrollPosition = {
    x: window.scrollX || window.pageXOffset,
    y: window.scrollY || window.pageYOffset
  };
  
  // 保存滚动位置（无论是否为0）
  sessionStorage.setItem('dashboard_scroll_position', JSON.stringify(scrollPosition));
  console.log('📍 保存滚动位置:', scrollPosition);
};

/**
 * 监听滚动事件，实时保存滚动位置
 */
const handleScroll = () => {
  // 使用防抖，避免频繁保存
  if (scrollSaveTimer) {
    clearTimeout(scrollSaveTimer);
  }
  scrollSaveTimer = setTimeout(saveScrollPosition, 150);
};

// 用户信息
const userInfo = ref({
  name: '用户',
  avatar: '',
  department: '技术部',
  position: '前端开发工程师'
});

// 当前日期
const currentDate = computed(() => {
  const now = new Date();
  const options = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    weekday: 'long'
  };
  return now.toLocaleDateString('zh-CN', options);
});

// 最新公告数据
const announcements = ref([]);
const loadingAnnouncements = ref(false);

// 个人统计数据
const personalStats = ref({
  monthlyLeave: 0,
  remainingAnnualLeave: 15,
  workYears: 0,
  joinDate: '2023-01',
  currentSalary: 0,
  salaryMonth: '本月'
});

// 个人统计数据加载状态
const statsLoading = ref(false);

// 获取个人统计数据
const fetchPersonalStats = async () => {
  try {
    statsLoading.value = true;
    console.log('🔄 获取个人统计数据');
    
    const currentEmpId = localStorage.getItem('user_employee_id');
    if (!currentEmpId) {
      console.warn('⚠️ 无法获取用户员工ID');
      return;
    }
    
    // 并行获取所有需要的数据
    const [employeeResult, leaveResult, salaryResult] = await Promise.allSettled([
      getEmployeeById(currentEmpId),
      getMyLeaveApplications(),
      getEmployeeSalaryHistory(currentEmpId)
    ]);
    
    // 处理员工信息
    let entryTime = '2023-01-15';
    let workYears = 1.0;
    let joinDate = '2023-01';
    
    if (employeeResult.status === 'fulfilled') {
      const employeeData = employeeResult.value?.data || employeeResult.value;
      entryTime = employeeData?.entryTime || employeeData?.entry_time || '2023-01-15';
      
      // 计算工作年限
      const entryDate = new Date(entryTime);
      const currentDate = new Date();
      workYears = Math.round((currentDate - entryDate) / (365.25 * 24 * 60 * 60 * 1000) * 10) / 10;
      joinDate = entryTime.substring(0, 7);
      
      console.log('👤 员工信息处理完成:', { entryTime, workYears, joinDate });
    } else {
      console.warn('⚠️ 获取员工信息失败:', employeeResult.reason);
    }
    
    // 处理请假数据
    let monthlyLeave = 0;
    let remainingAnnualLeave = 15;
    
    if (leaveResult.status === 'fulfilled') {
      const leaveData = leaveResult.value?.data || leaveResult.value || [];
      
      const currentMonth = new Date().getMonth() + 1;
      const currentYear = new Date().getFullYear();
      
      // 计算本月已批准的请假天数
      monthlyLeave = leaveData
        .filter(leave => {
          const startDate = new Date(leave.startDate);
          const isCurrentMonth = startDate.getMonth() + 1 === currentMonth && 
                                startDate.getFullYear() === currentYear;
          const isApproved = leave.status === '已批准' || leave.status === '批准';
          return isCurrentMonth && isApproved;
        })
        .reduce((total, leave) => total + (leave.days || 0), 0);
      
      // 计算本年度已使用的年假
      const usedAnnualLeave = leaveData
        .filter(leave => {
          const startDate = new Date(leave.startDate);
          const isCurrentYear = startDate.getFullYear() === currentYear;
          const isAnnualLeave = leave.type === '年假';
          const isApproved = leave.status === '已批准' || leave.status === '批准';
          return isCurrentYear && isAnnualLeave && isApproved;
        })
        .reduce((total, leave) => total + (leave.days || 0), 0);
      
      remainingAnnualLeave = Math.max(0, 15 - usedAnnualLeave);
      
      console.log('📅 请假数据处理完成:', { monthlyLeave, remainingAnnualLeave, usedAnnualLeave });
    } else {
      console.warn('⚠️ 获取请假数据失败:', leaveResult.reason);
    }
    
    // 处理薪资数据
    let currentSalary = 0;
    let salaryMonth = '暂无记录';
    let isCurrentMonth = false;
    
    if (salaryResult.status === 'fulfilled') {
      const salaryData = salaryResult.value?.data || salaryResult.value || [];
      
      if (salaryData.length > 0) {
        // 获取当前月份的薪资
        const currentYear = new Date().getFullYear();
        const currentMonth = new Date().getMonth() + 1;
        const currentMonthStr = `${currentYear}-${String(currentMonth).padStart(2, '0')}`;
        
        // 先尝试找当前月份的薪资
        let currentMonthSalary = salaryData.find(salary => salary.month === currentMonthStr);
        
        if (currentMonthSalary) {
          // 找到当前月份的薪资
          currentSalary = parseFloat(currentMonthSalary.totalSalary) || 0;
          salaryMonth = `${currentYear}年${currentMonth}月`;
          isCurrentMonth = true;
        } else {
          // 如果没有当前月份的薪资，取最新的一条但标明不是本月
          const sortedSalary = salaryData.sort((a, b) => b.month.localeCompare(a.month));
          currentMonthSalary = sortedSalary[0];
          currentSalary = parseFloat(currentMonthSalary.totalSalary) || 0;
          
          // 解析月份显示，明确标示这不是本月薪资
          const monthParts = currentMonthSalary.month.split('-');
          if (monthParts.length === 2) {
            salaryMonth = `最新：${monthParts[0]}年${parseInt(monthParts[1])}月`;
          } else {
            salaryMonth = `最新：${currentMonthSalary.month}`;
          }
          isCurrentMonth = false;
        }
      } else {
        // 没有任何薪资记录
        currentSalary = 0;
        salaryMonth = '暂无记录';
        isCurrentMonth = false;
      }
      
      console.log('💰 薪资数据处理完成:', { 
        currentSalary, 
        salaryMonth, 
        isCurrentMonth,
        totalRecords: salaryData.length 
      });
    } else {
      console.warn('⚠️ 获取薪资数据失败:', salaryResult.reason);
    }
    
    // 获取当前月份显示
    const now = new Date();
    const currentMonth = `${now.getFullYear()}年${now.getMonth() + 1}月`;
    
    // 一次性更新所有统计数据，避免闪烁
    personalStats.value = {
      monthlyLeave,
      remainingAnnualLeave,
      workYears,
      joinDate,
      currentSalary: currentSalary.toLocaleString(),
      salaryMonth,
      salaryTitle: isCurrentMonth ? '本月工资' : '薪资记录',
      currentMonth
    };
    
    console.log('✅ 个人统计数据获取成功:', personalStats.value);
    
  } catch (error) {
    console.error('❌ 获取个人统计数据失败:', error);
    // 获取当前月份显示（默认值时也需要）
    const now = new Date();
    const currentMonth = `${now.getFullYear()}年${now.getMonth() + 1}月`;
    
    // 使用默认值
    personalStats.value = {
      monthlyLeave: 0,
      remainingAnnualLeave: 15,
      workYears: 1.0,
      joinDate: '2023-01',
      currentSalary: '0',
      salaryMonth: '本月',
      currentMonth
    };
  } finally {
    statsLoading.value = false;
  }
};

// 获取最新公告
const fetchLatestAnnouncements = async () => {
  try {
    loadingAnnouncements.value = true;
    console.log('🔄 获取最新公告');
    
    // 调用真实API获取数据库中的公告数据
    const response = await getAnnouncements({ 
      pageSize: 5, // 只获取最新5条
      status: '已发布' // 只获取已发布的公告
    });
    
    console.log('✅ 公告API响应数据:', response);
    
    // 处理API响应数据
    let announcementData = [];
    if (Array.isArray(response)) {
      // 直接返回数组的情况
      announcementData = response;
    } else if (response && Array.isArray(response.data)) {
      // 分页数据结构
      announcementData = response.data;
    } else if (response && response.length !== undefined) {
      // 其他数组结构
      announcementData = response;
    } else {
      console.warn('⚠️ 未识别的API响应格式:', response);
      announcementData = [];
    }
    
    // 处理公告数据，确保格式正确
    let processedAnnouncements = announcementData.map(item => ({
      id: item.id,
      title: item.title || '无标题',
      content: item.content || '无内容',
      pubDate: item.pubDate || item.pub_date || new Date().toISOString().split('T')[0],
      status: item.status || '已发布',
      pubId: item.pubId || item.pub_id || 'admin'
    }));
    
    // 按发布时间倒序排列（最新公告在前）
    processedAnnouncements.sort((a, b) => {
      const dateA = new Date(a.pubDate);
      const dateB = new Date(b.pubDate);
      return dateB - dateA; // 倒序排列
    });
    
    // 只取最新的5条
    announcements.value = processedAnnouncements.slice(0, 5);
    
    console.log('✅ 最新公告数据处理完成（已按时间排序）:', announcements.value.map(a => ({
      id: a.id,
      title: a.title.substring(0, 20) + '...',
      pubDate: a.pubDate
    })));
    
  } catch (error) {
    console.error('❌ 获取公告失败:', error);
    // 发生错误时显示空状态
    announcements.value = [];
    ElMessage.warning('获取公告失败，请稍后重试');
  } finally {
    loadingAnnouncements.value = false;
  }
};

// 页面挂载时获取用户信息
onMounted(async () => {
  console.log('🎯 Dashboard 开始初始化');
  
  // 添加Dashboard页面标识，禁用smooth滚动
  document.body.classList.add('dashboard-page');
  
  // 获取用户信息
  try {
    const userStr = localStorage.getItem('user_info');
    if (userStr) {
      const userData = JSON.parse(userStr);
      userInfo.value.name = userData.name || userData.username || '用户';
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
  }
  
  // 获取数据
  await Promise.all([
    fetchPersonalStats(),
    fetchLatestAnnouncements()
  ]);
  
  // 添加滚动事件监听，实时保存滚动位置
  window.addEventListener('scroll', handleScroll, { passive: true });
  console.log('📍 已添加滚动位置监听');
});

// 组件卸载前清理事件监听
onBeforeUnmount(() => {
  // 移除Dashboard页面标识
  document.body.classList.remove('dashboard-page');
  
  // 移除滚动事件监听
  window.removeEventListener('scroll', handleScroll);
  
  // 清理定时器
  if (scrollSaveTimer) {
    clearTimeout(scrollSaveTimer);
    scrollSaveTimer = null;
  }
  
  console.log('🧹 已清理滚动位置监听');
});

// 导航方法
const goToProfile = () => {
  saveScrollPosition(); // 跳转前保存位置
  router.push('/employee/profile');
};

const goToSalary = () => {
  saveScrollPosition(); // 跳转前保存位置
  router.push('/employee/salary');
};

const goToAnnouncements = () => {
  // 跳转前保存当前滚动位置
  saveScrollPosition();
  router.push('/employee/announcements');
};

const goToLeave = () => {
  saveScrollPosition(); // 跳转前保存位置
  router.push('/employee/leave');
};

const viewAnnouncement = (announcement) => {
  console.log('查看公告:', announcement);
  // 跳转前保存当前滚动位置
  saveScrollPosition();
  router.push('/employee/announcements');
};
</script>

<style scoped>
.employee-dashboard {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}



.welcome-section {
  margin-bottom: 32px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  color: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h1 {
  font-size: 2rem;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.welcome-text p {
  font-size: 1.1rem;
  margin: 0;
  opacity: 0.9;
}

.welcome-avatar {
  flex-shrink: 0;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
}

.quick-actions {
  margin-bottom: 32px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.action-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  font-size: 24px;
  color: white;
}

.action-icon.profile { background: linear-gradient(135deg, #667eea, #764ba2); }
.action-icon.salary { background: linear-gradient(135deg, #f093fb, #f5576c); }
.action-icon.announcement { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.action-icon.leave { background: linear-gradient(135deg, #43e97b, #38f9d7); }

.action-card h3 {
  margin: 0 0 8px 0;
  font-size: 1.2rem;
  color: #2c3e50;
}

.action-card p {
  margin: 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: start;
}

.left-section,
.right-section {
  height: 100%;
}

.personal-stats,
.latest-announcements {
  height: 100%;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
  height: 480px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.stat-card.leave-stat {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
}

.stat-card.work-stat {
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
}

.stat-card.performance-stat {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-info h3 {
  margin: 0 0 4px 0;
  font-size: 1rem;
  color: white;
  font-weight: 600;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: bold;
  color: white;
  margin: 4px 0;
}

.stat-desc {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.8);
}

.announcements-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  height: 480px;
  overflow: hidden;
}

.announcements-card :deep(.el-card__body) {
  height: calc(100% - 60px);
  display: flex;
  flex-direction: column;
  padding: 20px;
  overflow: hidden;
}

.announcement-list {
  height: 360px;
  overflow-y: auto;
  flex: 1;
  padding-right: 8px;
}

.announcement-list::-webkit-scrollbar {
  width: 6px;
}

.announcement-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.announcement-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.announcement-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.view-all {
  text-align: center;
  padding-top: 16px;
  flex-shrink: 0;
}

.announcement-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.announcement-item:hover {
  background-color: #f8f9fa;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-content {
  flex: 1;
}

.announcement-content h4 {
  margin: 0 0 8px 0;
  font-size: 1rem;
  color: #2c3e50;
}

.announcement-content p {
  margin: 0 0 8px 0;
  color: #7f8c8d;
  font-size: 0.9rem;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.announcement-meta .date {
  font-size: 0.8rem;
  color: #95a5a6;
}

.arrow-icon {
  color: #bdc3c7;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .employee-dashboard {
    padding: 16px;
  }
  
  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .welcome-text h1 {
    font-size: 1.5rem;
  }
  
  .actions-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
  
  .main-content {
    grid-template-columns: 1fr;
  }
}
</style> 