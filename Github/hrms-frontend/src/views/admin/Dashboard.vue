<template>
  <div class="admin-dashboard">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <h1 class="page-title">管理员仪表板</h1>
      <p class="page-desc">欢迎回来，{{ adminName }}！这里是您的管理中心</p>
    </div>

    <!-- 统计卡片区域 -->
    <div class="stats-section">
      <div class="stats-grid">
        <!-- 员工总数 -->
        <div class="stat-card employees">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalEmployees }}</div>
            <div class="stat-label">员工总数</div>
            <div class="stat-change">
              <span>在职员工</span>
            </div>
          </div>
        </div>

        <!-- 月度薪资 -->
        <div class="stat-card salary">
          <div class="stat-icon">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">¥{{ formatNumber(stats.monthlySalary) }}</div>
            <div class="stat-label">本月薪资支出</div>
            <div class="stat-change">
              <span>{{ currentMonth }}</span>
            </div>
          </div>
        </div>

        <!-- 待处理申请 -->
        <div class="stat-card applications">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.pendingApplications }}</div>
            <div class="stat-label">待处理申请</div>
            <div class="stat-change">
              <span>需要处理</span>
              <small style="color: #666; font-size: 11px; display: block; margin-top: 4px;">
                更新: {{ lastUpdateTime }}
              </small>
            </div>
          </div>
        </div>

        <!-- 部门数量 -->
        <div class="stat-card departments">
          <div class="stat-icon">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ departmentCount }}</div>
            <div class="stat-label">部门数量</div>
            <div class="stat-change">
              <span>活跃部门</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作区域 -->
    <div class="quick-actions">
      <el-card class="actions-card">
        <template #header>
          <div class="card-header">
            <h3>快捷操作</h3>
            <el-button type="primary" size="small" @click="refreshData">
              <el-icon><RefreshRight /></el-icon>
              刷新数据
            </el-button>
          </div>
        </template>
        
        <div class="actions-grid">
          <div class="action-item" @click="navigateTo('/admin/employees')">
            <div class="action-icon employees">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="action-content">
              <h4>员工管理</h4>
              <p>管理员工信息、入职离职</p>
            </div>
          </div>

          <div class="action-item" @click="navigateTo('/admin/announcements')">
            <div class="action-icon announcements">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="action-content">
              <h4>公告管理</h4>
              <p>发布和管理公司公告</p>
            </div>
          </div>

          <div class="action-item" @click="navigateTo('/admin/leave-approval')">
            <div class="action-icon leave">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="action-content">
              <h4>请假审批</h4>
              <p>审批员工请假申请</p>
            </div>
          </div>

          <div class="action-item" @click="navigateTo('/admin/salary')">
            <div class="action-icon salary-mgmt">
              <el-icon><Money /></el-icon>
            </div>
            <div class="action-content">
              <h4>薪资管理</h4>
              <p>管理员工薪资发放</p>
            </div>
          </div>



          <div class="action-item" @click="navigateTo('/admin/settings')">
            <div class="action-icon settings">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="action-content">
              <h4>个人设置</h4>
              <p>管理个人信息和密码</p>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主要内容区域 - 新的网格布局 -->
    <div class="main-content">
      <!-- 月度薪资分析 - 占据左上和左下 -->
      <div class="chart-large">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <h3><el-icon><TrendCharts /></el-icon> 月度薪资数据分析</h3>
              <div class="chart-controls">
                <el-button-group>
                  <el-button 
                    :type="chartType === 'line' ? 'primary' : ''" 
                    size="small"
                    @click="switchChartType('line')"
                  >
                    <el-icon><TrendCharts /></el-icon>
                    线图
                  </el-button>
                  <el-button 
                    :type="chartType === 'bar' ? 'primary' : ''" 
                    size="small"
                    @click="switchChartType('bar')"
                  >
                    <el-icon><DataLine /></el-icon>
                    柱图
                  </el-button>
                </el-button-group>
              </div>
            </div>
          </template>
          <div ref="salaryChartRef" class="chart-container large-chart"></div>
        </el-card>
      </div>

      <!-- 部门人员分布 - 右上角，跨两列 -->
      <div class="chart-medium">
        <el-card class="chart-card">
          <template #header>
            <h3><el-icon><PieChart /></el-icon> 部门人员分布</h3>
          </template>
          <div ref="departmentChartRef" class="chart-container medium-chart"></div>
        </el-card>
      </div>

      <!-- 员工状态分布 - 右下左侧 -->
      <div class="chart-small">
        <el-card class="chart-card">
          <template #header>
            <h3><el-icon><Avatar /></el-icon> 员工状态分布</h3>
          </template>
          <div ref="employeeStatusChartRef" class="chart-container small-chart"></div>
        </el-card>
      </div>

      <!-- 系统信息 - 右下右侧 -->
      <div class="system-info">
        <el-card class="system-card">
          <template #header>
            <div class="card-header">
              <h3><el-icon><Setting /></el-icon> 系统信息</h3>
              <el-tag type="success" size="small">运行正常</el-tag>
            </div>
          </template>

          <div class="system-info-content">
            <div class="info-item">
              <div class="info-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="info-details">
                <span class="info-label">数据更新时间</span>
                <span class="info-value">{{ lastUpdateTime }}</span>
              </div>
            </div>
            
            <div class="info-item">
              <div class="info-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="info-details">
                <span class="info-label">系统状态</span>
                <el-tag type="success" size="small">正常运行</el-tag>
              </div>
            </div>
            
            <div class="info-item">
              <div class="info-icon">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="info-details">
                <span class="info-label">数据库连接</span>
                <el-tag type="success" size="small">已连接</el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import {
  User, Money, Document, UserFilled, Bell, 
  DataAnalysis, Setting, Calendar, OfficeBuilding,
  TrendCharts, DataLine, PieChart, Avatar, 
  Clock, CircleCheck, Connection, RefreshRight
} from '@element-plus/icons-vue';
import { getDashboardStats, getMonthlyTrends } from '@/api/admin';
import { getEmployeeList } from '@/api/employee';

// 路由实例
const router = useRouter();

// 响应式数据定义
const adminName = ref('管理员');
const chartType = ref('line');
const lastUpdateTime = ref('');

// 图表DOM引用
const salaryChartRef = ref();
const departmentChartRef = ref();
const employeeStatusChartRef = ref();

// 图表实例
let salaryChart = null;
let departmentChart = null;
let employeeStatusChart = null;

// 定时刷新定时器
let refreshTimer = null;

// 统计数据
const stats = reactive({
  totalEmployees: 0,
  monthlySalary: 0,
  pendingApplications: 0,
  departmentStats: {}
});

// 月度数据
const monthlyData = reactive({
  months: [],
  salaryTotals: []
});

// 员工状态统计数据
const employeeStatusData = reactive({
  total: 0,
  active: 0,      // 在职
  probation: 0,   // 试用期  
  resigned: 0     // 离职
});

// 计算属性
const departmentCount = computed(() => {
  return Object.keys(stats.departmentStats).length;
});

const currentMonth = computed(() => {
  const now = new Date();
  return `${now.getFullYear()}年${(now.getMonth() + 1).toString().padStart(2, '0')}月`;
});

/**
 * 格式化数字显示
 */
const formatNumber = (num) => {
  if (num === null || num === undefined) return '0';
  return Number(num).toLocaleString();
};

/**
 * 获取统计数据
 */
const fetchDashboardStats = async () => {
  try {
    console.log('🔍 开始获取仪表板统计数据...');
    console.log('⏰ 请求时间:', new Date().toLocaleString());
    console.log('🌐 API请求路径:', import.meta.env.VITE_API_BASE_URL + '/admin/dashboard/stats');
    
    // 强制获取最新数据，避免缓存
    const timestamp = Date.now();
    console.log('🚫 防缓存时间戳:', timestamp);
    
    // 直接获取真实数据
    const data = await getDashboardStats();
    console.log('📊 后端返回的原始数据:', data);
    console.log('🔢 待处理申请原始值:', data.pendingApplications, '类型:', typeof data.pendingApplications);
    
    // 如果数据不存在，立即抛出错误
    if (!data) {
      throw new Error('后端未返回任何数据');
    }
    
    // 确保数据格式正确并赋值
    stats.totalEmployees = Number(data.totalEmployees) || 0;
    stats.monthlySalary = Number(data.monthlySalary) || 0;
    stats.pendingApplications = Number(data.pendingApplications) || 0;
    stats.departmentStats = data.departmentStats || {};
    
    // 更新最后更新时间
    lastUpdateTime.value = new Date().toLocaleString();
    
    console.log('✅ 仪表板统计数据更新成功:', {
      totalEmployees: stats.totalEmployees,
      monthlySalary: stats.monthlySalary,
      pendingApplications: stats.pendingApplications,
      departmentCount: Object.keys(stats.departmentStats).length,
      departmentStats: stats.departmentStats
    });
    
    // 特别检查待处理申请数据
    console.log('🔍 待处理申请详细信息:', {
      原始数据: data.pendingApplications,
      处理后数据: stats.pendingApplications,
      数据类型: typeof stats.pendingApplications,
      是否从API获取: !!data.pendingApplications
    });
    
    // 如果数据为空，显示警告
    if (stats.totalEmployees === 0 && stats.monthlySalary === 0 && stats.pendingApplications === 0) {
      console.warn('⚠️ 所有统计数据都为0，请检查后端接口和数据库');
      ElMessage.warning('统计数据为空，请检查系统数据');
    }
    
  } catch (error) {
    console.error('❌ 获取仪表板统计数据失败:', error);
    console.error('错误详情:', error.response?.data || error.message);
    ElMessage.error('获取统计数据失败: ' + (error.response?.data?.message || error.message));
    
    // 只有在API完全失败时才使用空数据作为后备
    stats.totalEmployees = 0;
    stats.monthlySalary = 0;
    stats.pendingApplications = 0;
    stats.departmentStats = {};
  }
};

/**
 * 获取月度趋势数据
 */
const fetchMonthlyTrends = async () => {
  try {
    console.log('🔍 开始获取月度趋势数据...');
    
    // 直接获取真实数据
    const data = await getMonthlyTrends();
    console.log('📈 月度趋势原始数据:', data);
    
    monthlyData.months = Array.isArray(data.months) ? data.months : [];
    monthlyData.salaryTotals = Array.isArray(data.salaryTotals) ? data.salaryTotals : [];
    
    // 确保数据格式正确
    if (monthlyData.salaryTotals.length > 0) {
      monthlyData.salaryTotals = monthlyData.salaryTotals.map(total => Number(total) || 0);
    }
    
    console.log('✅ 月度趋势数据更新成功:', {
      months: monthlyData.months,
      salaryTotals: monthlyData.salaryTotals,
      dataCount: monthlyData.months.length
    });
    
    if (monthlyData.months.length === 0) {
      console.warn('⚠️ 月度趋势数据为空');
      ElMessage.warning('月度趋势数据为空，请检查系统数据');
    }
    
  } catch (error) {
    console.error('❌ 获取月度趋势数据失败:', error);
    console.error('错误详情:', error.response?.data || error.message);
    ElMessage.error('获取月度数据失败: ' + (error.response?.data?.message || error.message));
    
    // 只有在API完全失败时才使用空数据作为后备
    monthlyData.months = [];
    monthlyData.salaryTotals = [];
  }
};

/**
 * 获取员工状态统计数据
 */
const fetchEmployeeStatusStats = async () => {
  try {
    console.log('🔍 开始获取员工状态统计数据...');
    
    // 获取所有员工数据（不分页）
    const response = await getEmployeeList({ pageSize: 10000 });
    const employees = response.data || [];
    
    console.log('👥 获取到员工数据:', employees.length, '人');
    
    // 重置统计数据
    employeeStatusData.total = employees.length;
    employeeStatusData.active = 0;
    employeeStatusData.probation = 0;
    employeeStatusData.resigned = 0;
    
    // 统计各种状态的员工数量
    employees.forEach(employee => {
      const status = employee.status || '在职';
      console.log(`员工 ${employee.empId || employee.id} 状态:`, status);
      
      switch (status) {
        case '在职':
          employeeStatusData.active++;
          break;
        case '试用期':
          employeeStatusData.probation++;
          break;
        case '离职':
        case '已离职':
          employeeStatusData.resigned++;
          break;
        default:
          // 默认认为是在职状态
          employeeStatusData.active++;
          console.warn(`未知员工状态: ${status}，默认为在职`);
      }
    });
    
    console.log('✅ 员工状态统计完成:', {
      总数: employeeStatusData.total,
      在职: employeeStatusData.active,
      试用期: employeeStatusData.probation,
      离职: employeeStatusData.resigned
    });
    
    // 验证数据一致性
    const calculatedTotal = employeeStatusData.active + employeeStatusData.probation + employeeStatusData.resigned;
    if (calculatedTotal !== employeeStatusData.total) {
      console.warn('⚠️ 员工状态统计数据不一致:', {
        实际总数: employeeStatusData.total,
        计算总数: calculatedTotal
      });
    }
    
  } catch (error) {
    console.error('❌ 获取员工状态统计数据失败:', error);
    ElMessage.error('获取员工状态数据失败: ' + (error.message || '未知错误'));
    
    // 失败时重置数据
    employeeStatusData.total = 0;
    employeeStatusData.active = 0;
    employeeStatusData.probation = 0;
    employeeStatusData.resigned = 0;
  }
};

/**
 * 刷新数据
 */
const refreshData = async () => {
  await Promise.all([
    fetchDashboardStats(),
    fetchMonthlyTrends(),
    fetchEmployeeStatusStats()
  ]);
  
  // 重新初始化图表
  await nextTick();
  initCharts();
  
  ElMessage.success('数据刷新成功');
};

/**
 * 页面挂载时执行
 */
onMounted(async () => {
  console.log('📱 Dashboard组件开始挂载');
  console.log('🌐 当前API基础路径:', import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api');
  
  // 从本地存储获取管理员信息
  const userName = localStorage.getItem('user_name');
  const userRole = localStorage.getItem('user_role');
  const authToken = localStorage.getItem('auth_token');
  
  console.log('👤 用户信息:', { userName, userRole, hasToken: !!authToken });
  
  if (userName) {
    adminName.value = userName;
  }

  // 检查权限 - 支持大小写不敏感
  if (!userRole || userRole.toUpperCase() !== 'ADMIN') {
    console.warn('⚠️ 用户角色不是管理员:', userRole);
    ElMessage.error('您没有访问管理员面板的权限');
    return;
  }

  // 获取数据
  await refreshData();
  
  // 设置定时刷新（每30秒刷新一次统计数据）
  refreshTimer = setInterval(async () => {
    console.log('🔄 定时刷新仪表板统计数据...');
    await fetchDashboardStats();
  }, 30000);
  
  // 等待DOM更新后再初始化图表
  await nextTick();
  
  // 添加延迟确保DOM完全渲染
  setTimeout(() => {
    console.log('🎯 开始初始化图表');
    console.log('DOM引用状态:', {
      salaryChart: !!salaryChartRef.value,
      departmentChart: !!departmentChartRef.value,
      employeeStatusChart: !!employeeStatusChartRef.value
    });
    
    if (salaryChartRef.value && departmentChartRef.value && employeeStatusChartRef.value) {
      initCharts();
      console.log('✅ 图表初始化完成');
    } else {
      console.error('❌ 图表DOM元素未准备好');
      // 再次尝试
      setTimeout(() => {
        initCharts();
      }, 1000);
    }
  }, 500);
});

// 组件卸载时清理资源
onUnmounted(() => {
  console.log('🧹 清理图表资源');
  if (salaryChart) {
    salaryChart.dispose();
    salaryChart = null;
  }
  if (departmentChart) {
    departmentChart.dispose();
    departmentChart = null;
  }
  if (employeeStatusChart) {
    employeeStatusChart.dispose();
    employeeStatusChart = null;
  }
  
  // 清理定时器
  if (refreshTimer) {
    clearInterval(refreshTimer);
    refreshTimer = null;
    console.log('🧹 定时刷新器已清理');
  }
});

/**
 * 初始化所有图表
 */
const initCharts = () => {
  console.log('🎨 初始化所有图表');
  try {
    initSalaryChart();
    initDepartmentChart();
    initEmployeeStatusChart();
    console.log('✅ 所有图表初始化成功');
  } catch (error) {
    console.error('❌ 图表初始化失败:', error);
  }
};

/**
 * 初始化薪资数据分析图表
 */
const initSalaryChart = () => {
  console.log('📊 初始化薪资图表');
  
  if (!salaryChartRef.value) {
    console.error('❌ 薪资图表DOM元素不存在');
    return;
  }
  
  try {
    // 销毁旧图表
    if (salaryChart) {
      salaryChart.dispose();
    }
    
    salaryChart = echarts.init(salaryChartRef.value);
    console.log('✅ 薪资图表实例创建成功');
    console.log('📈 月度数据:', monthlyData);
    
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        },
        formatter: function(params) {
          const data = params[0];
          return `${data.name}<br/>薪资支出: ¥${Number(data.value).toLocaleString()}`;
        }
      },
      grid: {
        left: '5%',
        right: '5%',
        bottom: '10%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: monthlyData.months,
        axisLabel: {
          color: '#666'
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: function(value) {
            return '¥' + (value / 1000).toFixed(0) + 'k';
          },
          color: '#666'
        }
      },
      series: [
        {
          name: '薪资支出',
          type: chartType.value,
          data: monthlyData.salaryTotals,
          itemStyle: {
            color: '#67C23A'
          },
          lineStyle: {
            color: '#67C23A',
            width: 3
          },
          smooth: chartType.value === 'line'
        }
      ]
    };
    
    salaryChart.setOption(option);
    console.log('✅ 薪资图表配置设置成功');
  } catch (error) {
    console.error('❌ 薪资图表初始化失败:', error);
  }
};

/**
 * 初始化部门分布图表
 */
const initDepartmentChart = () => {
  console.log('🏢 初始化部门分布图表');
  
  if (!departmentChartRef.value) {
    console.error('❌ 部门图表DOM元素不存在');
    return;
  }
  
  try {
    // 销毁旧图表
    if (departmentChart) {
      departmentChart.dispose();
    }
    
    departmentChart = echarts.init(departmentChartRef.value);
    console.log('✅ 部门图表实例创建成功');
    console.log('🏢 部门数据:', stats.departmentStats);
    
    // 转换部门数据为图表格式
    const departmentData = Object.entries(stats.departmentStats).map(([name, value], index) => {
      const colors = ['#3498db', '#27ae60', '#e74c3c', '#f39c12', '#9b59b6', '#1abc9c', '#34495e', '#e67e22'];
      return {
        value: value,
        name: name,
        itemStyle: { color: colors[index % colors.length] }
      };
    });
    
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c}人 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: '5%',
        top: 'center',
        itemWidth: 10,
        itemHeight: 10,
        textStyle: {
          fontSize: 11,
          color: '#666'
        }
      },
      series: [
        {
          name: '部门人员',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['65%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 4,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 12,
              fontWeight: 'bold',
              formatter: '{b}\n{c}人'
            },
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          labelLine: {
            show: false
          },
          data: departmentData
        }
      ]
    };
    
    departmentChart.setOption(option);
    console.log('✅ 部门图表配置设置成功');
  } catch (error) {
    console.error('❌ 部门图表初始化失败:', error);
  }
};

/**
 * 初始化员工状态分布图表
 */
const initEmployeeStatusChart = () => {
  console.log('👥 初始化员工状态图表');
  
  if (!employeeStatusChartRef.value) {
    console.error('❌ 员工状态图表DOM元素不存在');
    return;
  }
  
  try {
    // 销毁旧图表
    if (employeeStatusChart) {
      employeeStatusChart.dispose();
    }
    
    employeeStatusChart = echarts.init(employeeStatusChartRef.value);
    console.log('✅ 员工状态图表实例创建成功');
    
    // 使用从API获取的真实员工状态数据
    console.log('👥 员工状态数据:', {
      总员工: employeeStatusData.total,
      在职: employeeStatusData.active,
      试用期: employeeStatusData.probation,
      离职: employeeStatusData.resigned
    });
    
    // 过滤掉数量为0的状态，避免图表显示空数据
    const chartData = [];
    if (employeeStatusData.active > 0) {
      chartData.push({ 
        value: employeeStatusData.active, 
        name: '在职',
        itemStyle: { color: '#67C23A' }
      });
    }
    if (employeeStatusData.probation > 0) {
      chartData.push({ 
        value: employeeStatusData.probation, 
        name: '试用期',
        itemStyle: { color: '#E6A23C' }
      });
    }
    if (employeeStatusData.resigned > 0) {
      chartData.push({ 
        value: employeeStatusData.resigned, 
        name: '离职',
        itemStyle: { color: '#F56C6C' }
      });
    }
    
    // 如果没有数据，显示空状态
    if (chartData.length === 0) {
      chartData.push({
        value: 1,
        name: '暂无数据',
        itemStyle: { color: '#d0d0d0' }
      });
    }
    
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: function(params) {
          if (params.name === '暂无数据') {
            return '暂无员工数据';
          }
          return `员工状态 <br/>${params.name}: ${params.value}人 (${params.percent}%)`;
        }
      },
      legend: {
        orient: 'vertical',
        left: '5%',
        top: 'center',
        itemWidth: 10,
        itemHeight: 10,
        textStyle: {
          fontSize: 11,
          color: '#666'
        }
      },
      series: [
        {
          name: '员工状态',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['65%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 4,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 12,
              fontWeight: 'bold',
              formatter: function(params) {
                if (params.name === '暂无数据') {
                  return '暂无数据';
                }
                return `${params.name}\n${params.value}人`;
              }
            },
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          labelLine: {
            show: false
          },
          data: chartData
        }
      ]
    };
    
    employeeStatusChart.setOption(option);
    console.log('✅ 员工状态图表配置设置成功');
  } catch (error) {
    console.error('❌ 员工状态图表初始化失败:', error);
  }
};

/**
 * 切换图表类型
 */
const switchChartType = (type) => {
  chartType.value = type;
  // 重新初始化薪资图表
  if (salaryChart) {
    initSalaryChart();
  }
};

/**
 * 导航到指定页面
 */
const navigateTo = (path) => {
  router.push(path);
};
</script>

<style lang="scss" scoped>
/* 页面容器样式 */
.admin-dashboard {
  width: 100%;
  margin: 0 auto;
  padding: 24px;
  /* 修复高DPI显示器滚动问题 */
  position: relative;
  min-height: 100%;
  box-sizing: border-box;
  /* 确保页面顶部内容可见 - 为固定头部留出空间 */
  padding-top: 24px; /* 减少顶部间距，因为App.vue已经有padding-top */
  /* 修复高DPI显示器布局问题 */
  display: flex;
  flex-direction: column;
  overflow: visible;
}

/* 页面标题区域 */
.page-header {
  margin-bottom: 32px;
  /* 确保在高DPI显示器下可见 */
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  
  .page-title {
    font-size: 2rem;
    font-weight: 700;
    color: #2c3e50;
    margin-bottom: 8px;
    /* 修复高DPI显示器文字渲染 */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }
  
  .page-desc {
    color: #7f8c8d;
    font-size: 1.1rem;
    margin: 0;
    /* 修复高DPI显示器文字渲染 */
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }
}

/* 统计卡片区域 */
.stats-section {
  margin-bottom: 32px;
  /* 确保在高DPI显示器下正确显示 */
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    
    .stat-card {
      display: flex;
      align-items: center;
      padding: 24px;
      background: white;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      transition: all 0.3s ease;
      /* 修复高DPI显示器渲染问题 */
      transform: translateZ(0);
      -webkit-transform: translateZ(0);
      
      &:hover {
        transform: translateY(-4px) translateZ(0);
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
      }
      
      .stat-icon {
        width: 70px;
        height: 70px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 2rem;
        color: white;
        margin-right: 20px;
        /* 修复高DPI显示器图标渲染 */
        transform: translateZ(0);
        -webkit-transform: translateZ(0);
      }
      
      .stat-content {
        flex: 1;
        
        .stat-number {
          font-size: 2.2rem;
          font-weight: 700;
          color: #2c3e50;
          line-height: 1;
          margin-bottom: 4px;
          /* 修复高DPI显示器文字渲染 */
          -webkit-font-smoothing: antialiased;
          -moz-osx-font-smoothing: grayscale;
        }
        
        .stat-label {
          font-size: 1rem;
          color: #7f8c8d;
          margin-bottom: 8px;
          /* 修复高DPI显示器文字渲染 */
          -webkit-font-smoothing: antialiased;
          -moz-osx-font-smoothing: grayscale;
        }
        
        .stat-change {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 0.9rem;
          font-weight: 500;
          
          &.positive {
            color: #27ae60;
          }
          
          &.negative {
            color: #e74c3c;
          }
        }
      }
      
      &.employees .stat-icon {
        background: linear-gradient(135deg, #667eea, #764ba2);
      }
      
      &.salary .stat-icon {
        background: linear-gradient(135deg, #4facfe, #00f2fe);
      }
      
      &.applications .stat-icon {
        background: linear-gradient(135deg, #43e97b, #38f9d7);
      }
      
      &.departments .stat-icon {
        background: linear-gradient(135deg, #e67e22, #e84393);
      }
    }
  }
}

/* 快捷操作区域 */
.quick-actions {
  margin-bottom: 32px;
  
  .actions-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    
    h3 {
      font-size: 1.3rem;
      font-weight: 600;
      color: #2c3e50;
      margin: 0;
    }
  }
  
  .actions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    
    .action-item {
      display: flex;
      align-items: center;
      padding: 20px;
      background: linear-gradient(135deg, #f8f9fa, #e9ecef);
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background: linear-gradient(135deg, #e9ecef, #dee2e6);
        transform: translateY(-2px);
      }
      
      .action-icon {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;
        color: white;
        margin-right: 16px;
        
        &.employees {
          background: linear-gradient(135deg, #667eea, #764ba2);
        }
        
        &.announcements {
          background: linear-gradient(135deg, #f093fb, #f5576c);
        }
        
        &.reports {
          background: linear-gradient(135deg, #4facfe, #00f2fe);
        }
        
        &.leave {
          background: linear-gradient(135deg, #fd79a8, #e84393);
        }
        
        &.salary-mgmt {
          background: linear-gradient(135deg, #fdcb6e, #e17055);
        }
        
        &.settings {
          background: linear-gradient(135deg, #43e97b, #38f9d7);
        }
      }
      
      .action-content {
        h4 {
          font-size: 1.1rem;
          font-weight: 600;
          color: #2c3e50;
          margin: 0 0 4px 0;
        }
        
        p {
          font-size: 0.9rem;
          color: #7f8c8d;
          margin: 0;
        }
      }
    }
  }
}

/* 主要内容区域 - 新的网格布局 */
.main-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
  height: 600px;
}

/* 月度薪资分析 - 占据左上和左下 */
.chart-large {
  grid-column: 1;
  grid-row: 1 / 3;
  
  .chart-card {
    height: 100%;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }
    
    .large-chart {
      height: 500px;
      min-height: 400px;
      width: 100%;
    }
  }
}

/* 部门人员分布 - 右上角，跨两列 */
.chart-medium {
  grid-column: 2 / 4;
  grid-row: 1;
  
  .chart-card {
    height: 100%;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }
    
    .medium-chart {
      height: 250px;
      min-height: 200px;
      width: 100%;
    }
  }
}

/* 员工状态分布 - 右下左侧 */
.chart-small {
  grid-column: 2;
  grid-row: 2;
  
  .chart-card {
    height: 100%;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 100%);
    
    &:hover {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
    }
    
    .small-chart {
      height: 200px;
      min-height: 150px;
      width: 100%;
    }
  }
}

/* 系统信息 - 右下右侧 */
.system-info {
  grid-column: 3;
  grid-row: 2;
  
  .system-card {
    height: 100%;
    min-height: 200px;
  }
}

/* 图表卡片通用样式 */
.chart-card {
  border: none;
  overflow: hidden;
  
  .el-card__header {
    background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 100%);
    color: #2c3e50;
    padding: 16px 20px;
    border-bottom: 1px solid #e0e0e0;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #2c3e50; /* 修改标题颜色为深色，确保可见 */
    }
  }
  
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h3 {
      color: #2c3e50; /* 修改标题颜色为深色，确保在白色背景上可见 */
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .chart-controls {
      .el-button-group {
        .el-button {
          border-color: #e0e0e0;
          color: #666;
          background: #f8f9fa;
          
          &:hover {
            background: #e9ecef;
            color: #333;
          }
          
          &.el-button--primary {
            background: #007bff;
            color: white;
            border-color: #007bff;
          }
        }
      }
    }
  }
}

/* 系统信息卡片样式 */
.system-card {
  height: 100%;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: none;
  
  .el-card__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 16px 20px;
    border-bottom: none;
    border-radius: 12px 12px 0 0;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: white;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
  }
  
  .el-card__body {
    background: white;
    margin: 0;
    border-radius: 0 0 12px 12px;
    padding: 20px;
  }
}

.system-info-content {
  padding: 16px 0;
  
  .info-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .info-icon {
      width: 32px;
      height: 32px;
      border-radius: 8px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      margin-right: 12px;
      font-size: 14px;
    }
    
    .info-details {
      flex: 1;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .info-label {
        font-size: 14px;
        color: #666;
        font-weight: 500;
      }
      
      .info-value {
        font-size: 14px;
        font-weight: 600;
        color: #2c3e50;
      }
    }
  }
}

/* 快捷操作区域优化 */
.quick-actions {
  margin-bottom: 32px;
  
  .actions-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    border: none;
    
    .el-card__header {
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      color: #2c3e50;
      border-bottom: 1px solid #dee2e6;
      border-radius: 12px 12px 0 0;
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        h3 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #2c3e50;
        }
        
        .el-button {
          background: #007bff;
          border-color: #007bff;
          color: white;
          
          &:hover {
            background: #0056b3;
            border-color: #0056b3;
          }
        }
      }
    }
  }
}

/* 图表容器通用样式 */
.chart-container {
  position: relative;
  overflow: hidden;
  
  &.large-chart,
  &.medium-chart,
  &.small-chart {
    background: transparent;
    border-radius: 8px;
  }
}

/* ==================== 高DPI显示器适配 ==================== */

/**
 * 高DPI显示器适配 (150%缩放及以上)
 */
@media screen and (-webkit-min-device-pixel-ratio: 1.5),
       screen and (min-resolution: 144dpi),
       screen and (min-resolution: 1.5dppx) {
  
  .admin-dashboard {
    /* 修复高DPI下的布局问题 */
    padding-top: 40px;
    transform: translateZ(0);
    -webkit-transform: translateZ(0);
    will-change: transform;
  }
  
  .page-header {
    /* 确保页面标题在高DPI下可见 */
    margin-bottom: 40px;
    padding-top: 8px;
    
    .page-title {
      font-size: 2.2rem;
    }
    
    .page-desc {
      font-size: 1.2rem;
    }
  }
  
  .stats-section {
    /* 确保统计卡片在高DPI下正确显示 */
    margin-bottom: 40px;
    
    .stats-grid {
      gap: 24px;
      
      .stat-card {
        padding: 28px;
        
        .stat-icon {
          width: 80px;
          height: 80px;
          font-size: 2.2rem;
        }
        
        .stat-content .stat-number {
          font-size: 2.4rem;
        }
      }
    }
  }
}

/**
 * 特定分辨率适配 (2560x1600 @ 150%缩放)
 */
@media screen and (min-width: 2560px) and (min-height: 1600px) {
  .admin-dashboard {
    /* 确保在高分辨率下页面顶部内容可见 */
    padding-top: 48px;
    max-width: none;
    width: 100%;
  }
  
  .page-header {
    /* 增加页面标题的上边距 */
    padding-top: 16px;
    margin-bottom: 48px;
    
    .page-title {
      font-size: 2.4rem;
      margin-bottom: 12px;
    }
    
    .page-desc {
      font-size: 1.3rem;
    }
  }
  
  .stats-section {
    margin-bottom: 48px;
    
    .stats-grid {
      gap: 28px;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    }
  }
}

/* 响应式设计 */

/* 大屏幕显示系统信息 */
@media (min-width: 1400px) {
  .main-content {
    grid-template-columns: 2fr 1fr 1fr;
    grid-template-rows: 2fr 1fr;
    height: 650px;
  }
  
  .chart-large {
    grid-column: 1;
    grid-row: 1 / 3;
  }
  
  .chart-medium {
    grid-column: 2;
    grid-row: 1;
  }
  
  .chart-small {
    grid-column: 3;
    grid-row: 1;
  }
  
  .system-info {
    display: block;
    grid-column: 2 / 4;
    grid-row: 2;
  }
}

/* 中等屏幕优化 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto;
    height: auto;
    gap: 20px;
  }
  
  .chart-large,
  .chart-medium,
  .chart-small {
    grid-column: 1;
    grid-row: auto;
  }
  
  .chart-large .large-chart,
  .chart-medium .medium-chart,
  .chart-small .small-chart {
    height: 300px;
  }
  
  .system-info {
    display: block;
    grid-column: 1;
    grid-row: auto;
  }
}

/* 平板屏幕 */
@media (max-width: 768px) {
  .admin-dashboard {
    padding: 16px;
  }
  
  .page-header {
    .page-title {
      font-size: 1.8rem;
    }
    
    .page-desc {
      font-size: 1rem;
    }
  }
  
  .stats-grid {
    grid-template-columns: 1fr !important;
    gap: 16px;
  }
  
  .actions-grid {
    grid-template-columns: repeat(2, 1fr) !important;
    gap: 16px;
  }
  
  .main-content {
    gap: 16px;
  }
  
  .chart-large .large-chart,
  .chart-medium .medium-chart,
  .chart-small .small-chart {
    height: 250px;
    padding: 8px;
  }
  
  .system-info-content .info-item {
    padding: 8px 0;
    
    .info-icon {
      width: 28px;
      height: 28px;
      font-size: 12px;
    }
    
    .info-details {
      .info-label,
      .info-value {
        font-size: 13px;
      }
    }
  }
}

/* 手机屏幕 */
@media (max-width: 480px) {
  .actions-grid {
    grid-template-columns: 1fr !important;
  }
  
  .stat-card {
    flex-direction: column !important;
    text-align: center;
    
    .stat-icon {
      margin-right: 0 !important;
      margin-bottom: 12px;
    }
  }
  
  .chart-large .large-chart,
  .chart-medium .medium-chart,
  .chart-small .small-chart {
    height: 200px;
  }
  
  .chart-card .el-card__header h3 {
    font-size: 14px;
  }
  
  .chart-header .chart-controls {
    .el-button-group .el-button {
      font-size: 12px;
      padding: 4px 8px;
    }
  }
}
</style> 
