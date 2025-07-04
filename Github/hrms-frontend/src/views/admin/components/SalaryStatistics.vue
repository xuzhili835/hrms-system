<template>
  <div class="salary-statistics">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon total">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">¥{{ totalSalary.toLocaleString() }}</div>
              <div class="stat-label">总薪资支出</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon average">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">¥{{ averageSalary.toLocaleString() }}</div>
              <div class="stat-label">平均薪资</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon highest">
              <el-icon><Top /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">¥{{ highestSalary.toLocaleString() }}</div>
              <div class="stat-label">最高薪资</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon employees">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ totalEmployees }}</div>
              <div class="stat-label">员工总数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- 部门薪资分布 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>部门薪资分布</span>
                <el-button size="small" @click="refreshDepartmentChart">刷新</el-button>
              </div>
            </template>
            <div ref="departmentChartRef" class="chart-container" v-loading="loading">
              <div v-if="!departmentChart && !loading" class="chart-placeholder">
                <el-icon><Warning /></el-icon>
                <p>图表加载失败，请点击刷新重试</p>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 员工薪资等级分布 -->
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>员工薪资等级分布</span>
                <el-button size="small" @click="refreshSalaryChart">刷新</el-button>
              </div>
            </template>
            <div ref="salaryChartRef" class="chart-container" v-loading="loading">
              <div v-if="!salaryChart && !loading" class="chart-placeholder">
                <el-icon><Warning /></el-icon>
                <p>图表加载失败，请点击刷新重试</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 月度薪资趋势 -->
        <el-col :span="24">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>月度薪资趋势</span>
                <div class="header-controls">
                  <span class="filter-info" v-if="trendDateRange && trendDateRange.length === 2">
                    筛选范围: {{ trendDateRange[0] }} 至 {{ trendDateRange[1] }}
                  </span>
                  
                  <!-- 快速筛选按钮 -->
                  <div class="quick-filters">
                    <el-button size="small" @click="setQuickDateRange(3)">最近3个月</el-button>
                    <el-button size="small" @click="setQuickDateRange(6)">最近6个月</el-button>
                    <el-button size="small" @click="setQuickDateRange(12)">最近12个月</el-button>
                  </div>
                  
                  <!-- 日期选择器 -->
                  <el-date-picker
                    v-model="trendDateRange"
                    type="monthrange"
                    range-separator="至"
                    start-placeholder="开始月份"
                    end-placeholder="结束月份"
                    format="YYYY-MM"
                    value-format="YYYY-MM"
                    @change="updateTrendChart"
                    clearable
                    size="small"
                  />
                  
                  <el-button size="small" @click="clearDateFilter" v-if="trendDateRange && trendDateRange.length === 2">
                    清除筛选
                  </el-button>
                  <el-button size="small" @click="refreshTrendChart">刷新</el-button>
                </div>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container trend-chart" v-loading="loading">
              <div v-if="!trendChart && !loading" class="chart-placeholder">
                <el-icon><Warning /></el-icon>
                <p>图表加载失败，请点击刷新重试</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细统计表格 -->
    <div class="detailed-stats">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>部门薪资详情</span>
          </div>
        </template>
        
        <el-table :data="departmentStats" stripe style="width: 100%" border>
          <el-table-column prop="department" label="部门" min-width="120" align="center">
            <template #default="{ row }">
              <div class="department-cell">
                <el-tag :type="getDepartmentTagType(row.department)">
                  {{ row.department }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="employeeCount" label="员工数量" min-width="100" align="center">
            <template #default="{ row }">
              <div class="count-cell">
                <span class="count-number">{{ row.employeeCount }}</span>
                <span class="count-unit">人</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="totalSalary" label="总薪资" min-width="150" align="right">
            <template #default="{ row }">
              <div class="salary-cell">
                <span class="salary-amount total">¥{{ formatSalary(row.totalSalary) }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="averageSalary" label="平均薪资" min-width="150" align="right">
            <template #default="{ row }">
              <div class="salary-cell">
                <span class="salary-amount average">¥{{ formatSalary(row.averageSalary) }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="highestSalary" label="最高薪资" min-width="150" align="right">
            <template #default="{ row }">
              <div class="salary-cell">
                <span class="salary-amount highest">¥{{ formatSalary(row.highestSalary) }}</span>
                <el-icon class="trend-icon"><Top /></el-icon>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="lowestSalary" label="最低薪资" min-width="150" align="right">
            <template #default="{ row }">
              <div class="salary-cell">
                <span class="salary-amount lowest">¥{{ formatSalary(row.lowestSalary) }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="salaryRatio" label="薪资占比" min-width="180" align="center">
            <template #default="{ row }">
              <div class="ratio-cell">
                <el-progress 
                  :percentage="row.salaryRatio" 
                  :stroke-width="12"
                  :show-text="true"
                  :format="(percentage) => `${percentage}%`"
                  :color="getProgressColor(row.salaryRatio)"
                />
                <div class="ratio-desc">占总薪资比例</div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { Money, TrendCharts, Top, User, Warning } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { getSalaryStatistics, getMonthlySalaryTrend, getSalaryLevelDistribution } from '@/api/salary';

// 图表引用
const departmentChartRef = ref();
const salaryChartRef = ref();
const trendChartRef = ref();

// 图表实例
let departmentChart = null;
let salaryChart = null;
let trendChart = null;

// 响应式数据
const loading = ref(false);
const trendDateRange = ref([]);

// 统计数据
const totalSalary = ref(0);
const averageSalary = ref(0);
const highestSalary = ref(0);
const totalEmployees = ref(0);
const departmentStats = ref([]);

// 获取统计数据
const fetchStatistics = async () => {
  try {
    loading.value = true;
    const response = await getSalaryStatistics();
    
    console.log('📊 获取到的统计数据:', response);
    
    // 更新统计卡片数据
    totalSalary.value = Number(response.totalSalary) || 0;
    averageSalary.value = Number(response.averageSalary) || 0;
    highestSalary.value = Number(response.highestSalary) || 0;
    totalEmployees.value = Number(response.totalEmployees) || 0;
    
    // 确保departmentStats是数组
    if (Array.isArray(response.departmentStats)) {
      departmentStats.value = response.departmentStats;
    } else {
      console.warn('⚠️ departmentStats不是数组格式:', response.departmentStats);
      departmentStats.value = [];
    }
    
    // 等待DOM更新
    await nextTick();
    
    // 延迟初始化图表，确保DOM完全渲染
    setTimeout(async () => {
      await initChartsWithRetry();
      await updateCharts(response);
    }, 500);
    
  } catch (error) {
    console.error('❌ 获取统计数据失败:', error);
    ElMessage.error('获取统计数据失败');
  } finally {
    loading.value = false;
  }
};

// 带重试机制的图表初始化
const initChartsWithRetry = async (maxRetries = 10) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      // 等待一段时间让DOM完全渲染
      await new Promise(resolve => setTimeout(resolve, 100 * (i + 1)));
      
      const success = initCharts();
      if (success) {
        console.log(`✅ 图表初始化成功 (第${i + 1}次尝试)`);
        return true;
      }
      
      console.log(`⏳ 图表初始化第${i + 1}次尝试失败，继续重试...`);
    } catch (error) {
      console.error(`❌ 图表初始化第${i + 1}次尝试出错:`, error);
    }
  }
  
  console.error('❌ 图表初始化最终失败，已达到最大重试次数');
  return false;
};

// 响应式月度趋势数据
const monthlyTrendData = ref({ months: [], values: [] });

// 获取真实月度趋势数据
const getRealMonthlyTrendData = async () => {
  try {
    const response = await getMonthlySalaryTrend();
    const data = response.data || response;
    
    if (Array.isArray(data) && data.length > 0) {
      const months = data.map(item => {
        const monthStr = item.month; // YYYY-MM格式
        const [year, month] = monthStr.split('-');
        return `${year}年${parseInt(month)}月`;
      });
      
      const values = data.map(item => item.totalSalary || 0);
      
      // 缓存真实数据
      monthlyTrendData.value = { months, values };
      
      console.log('📈 获取真实月度趋势数据:', {
        dataLength: data.length,
        months: months,
        values: values
      });
      
      return monthlyTrendData.value;
    } else {
      console.warn('⚠️ 月度趋势数据为空，使用默认数据');
      return generateFallbackTrendData();
    }
  } catch (error) {
    console.error('❌ 获取月度趋势数据失败:', error);
    return generateFallbackTrendData();
  }
};

// 生成备用月度趋势数据（在API失败时使用）
const generateFallbackTrendData = () => {
  const months = [];
  const values = [];
  const currentDate = new Date();
  
  // 生成最近6个月的备用数据
  for (let i = 5; i >= 0; i--) {
    const date = new Date(currentDate.getFullYear(), currentDate.getMonth() - i, 1);
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    
    months.push(`${year}年${month}月`);
    values.push(Math.floor(500000 + Math.random() * 300000)); // 50万-80万随机数据
  }
  
  monthlyTrendData.value = { months, values };
  return monthlyTrendData.value;
};

// 重置月度趋势数据（用于刷新）
const resetMonthlyTrendData = () => {
  monthlyTrendData.value = { months: [], values: [] };
};

// 更新薪资等级分布图表
const updateSalaryLevelChart = async () => {
  try {
    const response = await getSalaryLevelDistribution();
    const data = response.data || response;
    
    if (Array.isArray(data) && data.length > 0) {
      const levels = data.map(item => item.level);
      const counts = data.map(item => item.count);
      
      const salaryOption = {
        title: {
          text: '员工薪资等级分布',
          left: 'center',
          textStyle: { fontSize: 14 }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: function(params) {
            const item = params[0];
            const dataIndex = item.dataIndex;
            const percentage = data[dataIndex].percentage || 0;
            return `${item.name}<br/>人数: ${item.value}<br/>占比: ${percentage}%`;
          }
        },
        xAxis: {
          type: 'category',
          data: levels,
          axisLabel: { rotate: 45 }
        },
        yAxis: {
          type: 'value',
          name: '人数'
        },
        series: [
          {
            name: '员工数量',
            type: 'bar',
            data: counts,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ])
            }
          }
        ]
      };
      salaryChart.setOption(salaryOption);
      console.log('✅ 薪资等级图表更新成功');
    } else {
      console.warn('⚠️ 薪资等级数据为空');
    }
  } catch (error) {
    console.error('❌ 更新薪资等级图表失败:', error);
    // 使用备用数据
    const fallbackOption = {
      title: {
        text: '员工薪资等级分布',
        left: 'center',
        textStyle: { fontSize: 14 }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      xAxis: {
        type: 'category',
        data: ['3K以下', '3K-5K', '5K-8K', '8K-12K', '12K-20K', '20K以上'],
        axisLabel: { rotate: 45 }
      },
      yAxis: {
        type: 'value',
        name: '人数'
      },
      series: [
        {
          name: '员工数量',
          type: 'bar',
          data: [5, 15, 25, 20, 12, 3],
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          }
        }
      ]
    };
    salaryChart.setOption(fallbackOption);
  }
};

// 更新月度薪资趋势图表数据
const updateTrendChartWithRealData = async () => {
  try {
    const monthlyData = await getRealMonthlyTrendData();
    
    const trendOption = {
      title: {
        text: '月度薪资趋势',
        left: 'center',
        textStyle: { fontSize: 14 }
      },
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          const month = params[0].name;
          const value = params[0].value;
          return `${month}<br/>总薪资: ¥${value.toLocaleString()}`;
        }
      },
      xAxis: {
        type: 'category',
        data: monthlyData.months,
        boundaryGap: false,
        axisLabel: {
          rotate: 45,
          fontSize: 11,
          interval: 0,
          margin: 8,
          color: '#666'
        },
        axisLine: {
          lineStyle: {
            color: '#e4e7ed'
          }
        },
        axisTick: {
          alignWithLabel: true,
          lineStyle: {
            color: '#e4e7ed'
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '薪资(元)',
        axisLabel: {
          formatter: function(value) {
            if (value >= 10000) {
              return (value / 10000) + 'W';
            }
            return value;
          }
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true
      },
      series: [
        {
          name: '总薪资',
          type: 'line',
          data: monthlyData.values,
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: {
            width: 3,
            color: '#409eff'
          },
          itemStyle: {
            color: '#409eff'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ])
          }
        }
      ]
    };
    trendChart.setOption(trendOption);
    console.log('✅ 趋势图表更新成功');
  } catch (error) {
    console.error('❌ 更新趋势图表失败:', error);
  }
};

// 验证时间排序是否正确
const validateTimeOrder = (months) => {
  console.log('🔍 验证月度数据时间排序:');
  
  for (let i = 0; i < months.length; i++) {
    const monthStr = months[i];
    const match = monthStr.match(/(\d{4})年(\d{1,2})月/);
    if (match) {
      const year = parseInt(match[1]);
      const month = parseInt(match[2]);
      console.log(`${i + 1}. ${monthStr} -> 排序值: ${year * 100 + month}`);
    }
  }
  
  // 检查是否按时间顺序排列
  let isOrdered = true;
  for (let i = 1; i < months.length; i++) {
    const prev = months[i - 1].match(/(\d{4})年(\d{1,2})月/);
    const curr = months[i].match(/(\d{4})年(\d{1,2})月/);
    
    if (prev && curr) {
      const prevSort = parseInt(prev[1]) * 100 + parseInt(prev[2]);
      const currSort = parseInt(curr[1]) * 100 + parseInt(curr[2]);
      
      if (prevSort >= currSort) {
        isOrdered = false;
        console.error(`❌ 时间排序错误: ${months[i - 1]} -> ${months[i]}`);
        break;
      }
    }
  }
  
  if (isOrdered) {
    console.log('✅ 时间排序验证通过');
  }
  
  return isOrdered;
};

// 格式化薪资显示
const formatSalary = (amount) => {
  if (!amount) return '0';
  const num = Number(amount);
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'W';
  }
  return num.toLocaleString();
};

// 获取部门标签类型
const getDepartmentTagType = (department) => {
  const typeMap = {
    '技术部': 'primary',
    '人事部': 'success',
    '财务部': 'warning',
    '市场部': 'info',
    '销售部': 'danger'
  };
  return typeMap[department] || '';
};

// 获取进度条颜色
const getProgressColor = (percentage) => {
  if (percentage >= 30) return '#f56c6c'; // 红色
  if (percentage >= 20) return '#e6a23c'; // 橙色
  if (percentage >= 15) return '#409eff'; // 蓝色
  return '#67c23a'; // 绿色
};

// 初始化图表
const initCharts = () => {
  let allSuccess = true;
  
  try {
    // 部门薪资分布饼图
    if (departmentChartRef.value && !departmentChart) {
      // 检查元素是否可见
      const rect = departmentChartRef.value.getBoundingClientRect();
      console.log('📏 部门图表容器尺寸:', rect);
      if (rect.width > 0 && rect.height > 0) {
        departmentChart = echarts.init(departmentChartRef.value);
        console.log('✅ 部门图表初始化成功');
      } else {
        console.warn('⚠️ 部门图表容器尺寸为0，跳过初始化');
        allSuccess = false;
      }
    }
    
    // 薪资等级分布柱状图
    if (salaryChartRef.value && !salaryChart) {
      const rect = salaryChartRef.value.getBoundingClientRect();
      console.log('📏 薪资图表容器尺寸:', rect);
      if (rect.width > 0 && rect.height > 0) {
        salaryChart = echarts.init(salaryChartRef.value);
        console.log('✅ 薪资图表初始化成功');
      } else {
        console.warn('⚠️ 薪资图表容器尺寸为0，跳过初始化');
        allSuccess = false;
      }
    }
    
    // 月度薪资趋势线图
    if (trendChartRef.value && !trendChart) {
      const rect = trendChartRef.value.getBoundingClientRect();
      console.log('📏 趋势图表容器尺寸:', rect);
      if (rect.width > 0 && rect.height > 0) {
        trendChart = echarts.init(trendChartRef.value);
        console.log('✅ 趋势图表初始化成功');
      } else {
        console.warn('⚠️ 趋势图表容器尺寸为0，跳过初始化');
        allSuccess = false;
      }
    }
  } catch (error) {
    console.error('❌ 图表初始化失败:', error);
    allSuccess = false;
  }
  
  return allSuccess;
};

// 更新图表数据
const updateCharts = async (data) => {
  try {
    // 检查数据格式
    if (!data || !Array.isArray(data.departmentStats)) {
      console.warn('⚠️ 数据格式不正确，跳过图表更新');
      return;
    }
    
    // 部门薪资分布饼图
    if (departmentChart) {
      const departmentOption = {
        title: {
          text: '部门薪资分布',
          left: 'center',
          textStyle: { fontSize: 14 }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle'
        },
        series: [
          {
            name: '薪资分布',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['60%', '50%'],
            data: data.departmentStats.map(item => ({
              value: item.totalSalary,
              name: item.department
            })),
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      };
      departmentChart.setOption(departmentOption);
      console.log('✅ 部门图表更新成功');
    }
    
    // 薪资等级分布柱状图 - 使用真实数据
    if (salaryChart) {
      await updateSalaryLevelChart();
    }
    
    // 月度薪资趋势线图 - 使用真实数据
    if (trendChart) {
      await updateTrendChartWithRealData();
    }
  } catch (error) {
    console.error('❌ 图表更新失败:', error);
  }
};

// 刷新图表
const refreshDepartmentChart = async () => {
  try {
    if (departmentChart) {
      departmentChart.dispose();
      departmentChart = null;
    }
    await nextTick();
    setTimeout(async () => {
      const success = await initChartsWithRetry();
      if (success) {
        const response = await getSalaryStatistics();
        await updateCharts(response);
        ElMessage.success('部门图表刷新成功');
      }
    }, 100);
  } catch (error) {
    console.error('刷新部门图表失败:', error);
    ElMessage.error('刷新失败');
  }
};

const refreshSalaryChart = async () => {
  try {
    if (salaryChart) {
      salaryChart.dispose();
      salaryChart = null;
    }
    await nextTick();
    setTimeout(async () => {
      const success = await initChartsWithRetry();
      if (success) {
        const response = await getSalaryStatistics();
        await updateCharts(response);
        ElMessage.success('薪资图表刷新成功');
      }
    }, 100);
  } catch (error) {
    console.error('刷新薪资图表失败:', error);
    ElMessage.error('刷新失败');
  }
};

const refreshTrendChart = async () => {
  try {
    // 重置趋势数据
    resetMonthlyTrendData();
    
    if (trendChart) {
      trendChart.dispose();
      trendChart = null;
    }
    await nextTick();
    setTimeout(async () => {
      const success = await initChartsWithRetry();
      if (success) {
        const response = await getSalaryStatistics();
        await updateCharts(response);
        ElMessage.success('趋势图表刷新成功');
      }
    }, 100);
  } catch (error) {
    console.error('刷新趋势图表失败:', error);
    ElMessage.error('刷新失败');
  }
};

// 设置默认日期范围（最近6个月）
const setDefaultDateRange = () => {
  const currentDate = new Date();
  const endDate = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`;
  
  const startDateObj = new Date(currentDate.getFullYear(), currentDate.getMonth() - 5, 1);
  const startDate = `${startDateObj.getFullYear()}-${String(startDateObj.getMonth() + 1).padStart(2, '0')}`;
  
  // 初始化时不设置默认范围，让用户手动选择
  trendDateRange.value = [];
  console.log('📅 日期筛选器已重置，等待用户选择');
};

// 根据日期范围筛选趋势数据
const getFilteredTrendData = async () => {
  // 获取完整的真实月度数据
  const fullData = await getRealMonthlyTrendData();
  
  // 如果没有选择日期范围，返回所有数据
  if (!trendDateRange.value || trendDateRange.value.length !== 2) {
    console.log('📊 无日期筛选，使用全部数据');
    return fullData;
  }
  
  const [startDate, endDate] = trendDateRange.value;
  console.log(`📅 筛选日期范围: ${startDate} 至 ${endDate}`);
  
  const filteredMonths = [];
  const filteredValues = [];
  
  // 遍历所有月份，筛选在范围内的数据
  fullData.months.forEach((monthStr, index) => {
    // 解析月份字符串 "2024年1月" -> "2024-01"
    const match = monthStr.match(/(\d{4})年(\d{1,2})月/);
    if (match) {
      const year = match[1];
      const month = String(match[2]).padStart(2, '0');
      const monthKey = `${year}-${month}`;
      
      // 检查是否在筛选范围内（包含边界）
      if (monthKey >= startDate && monthKey <= endDate) {
        filteredMonths.push(monthStr);
        filteredValues.push(fullData.values[index]);
      }
    }
  });
  
  console.log(`📈 筛选结果: ${filteredMonths.length} 个月份`, {
    startDate,
    endDate,
    months: filteredMonths,
    values: filteredValues
  });
  
  // 如果筛选后没有数据，提示用户
  if (filteredMonths.length === 0) {
    ElMessage.warning('筛选范围内没有数据，请调整日期范围');
    // 返回空数据而不是 fallback 数据
    return {
      months: [],
      values: []
    };
  }
  
  return {
    months: filteredMonths,
    values: filteredValues
  };
};

// 更新趋势图表数据
const updateTrendChartData = (data) => {
  if (!trendChart || !data) {
    console.warn('⚠️ 图表或数据不存在，无法更新');
    return;
  }
  
  // 如果没有数据，显示空状态
  if (data.months.length === 0) {
    const emptyOption = {
      title: {
        text: '月度薪资趋势',
        subtext: '暂无数据或筛选范围内无数据',
        left: 'center',
        textStyle: { fontSize: 14 },
        subtextStyle: { fontSize: 12, color: '#999' }
      },
      xAxis: {
        type: 'category',
        data: []
      },
      yAxis: {
        type: 'value',
        name: '薪资(元)'
      },
      series: [
        {
          name: '总薪资',
          type: 'line',
          data: []
        }
      ]
    };
    trendChart.setOption(emptyOption, true);
    console.log('📊 显示空状态图表');
    return;
  }
  
  const hasFilter = trendDateRange.value && trendDateRange.value.length === 2;
  const titleText = hasFilter 
    ? `月度薪资趋势 (筛选: ${trendDateRange.value[0]} 至 ${trendDateRange.value[1]})` 
    : '月度薪资趋势 (全部数据)';
  
  const trendOption = {
    title: {
      text: titleText,
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const month = params[0].name;
        const value = params[0].value;
        return `${month}<br/>总薪资: ¥${value.toLocaleString()}`;
      }
    },
    xAxis: {
      type: 'category',
      data: data.months,
      boundaryGap: false,
      axisLabel: {
        rotate: 45,
        fontSize: 11,
        interval: 0,
        margin: 8,
        color: '#666'
      },
      axisLine: {
        lineStyle: {
          color: '#e4e7ed'
        }
      },
      axisTick: {
        alignWithLabel: true,
        lineStyle: {
          color: '#e4e7ed'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '薪资(元)',
      axisLabel: {
        formatter: function(value) {
          if (value >= 10000) {
            return (value / 10000) + 'W';
          }
          return value;
        }
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    series: [
      {
        name: '总薪资',
        type: 'line',
        data: data.values,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 3,
          color: '#409eff'
        },
        itemStyle: {
          color: '#409eff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      }
    ]
  };
  
  trendChart.setOption(trendOption, true); // true表示不合并配置，完全替换
  console.log('✅ 趋势图表数据更新成功', { 
    dataCount: data.months.length, 
    hasFilter 
  });
};

// 更新趋势图表
const updateTrendChart = async () => {
  if (!trendChart) {
    console.warn('⚠️ 趋势图表未初始化，无法更新');
    return;
  }
  
  console.log('📅 日期范围变更:', trendDateRange.value);
  
  try {
    // 根据选择的日期范围筛选数据
    const filteredData = await getFilteredTrendData();
    
    // 更新图表数据
    updateTrendChartData(filteredData);
    
    // 只有在有筛选条件时才显示成功消息
    if (trendDateRange.value && trendDateRange.value.length === 2) {
      ElMessage.success(`已应用日期筛选: ${trendDateRange.value[0]} 至 ${trendDateRange.value[1]}`);
    }
  } catch (error) {
    console.error('❌ 更新趋势图表失败:', error);
    ElMessage.error('更新趋势图表失败');
  }
};

// 清除日期筛选
const clearDateFilter = async () => {
  trendDateRange.value = [];
  await updateTrendChart();
  ElMessage.success('已清除日期筛选，显示全部数据');
};

// 快速设置日期范围
const setQuickDateRange = (months) => {
  const currentDate = new Date();
  const endDate = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}`;
  
  const startDateObj = new Date(currentDate.getFullYear(), currentDate.getMonth() - months + 1, 1);
  const startDate = `${startDateObj.getFullYear()}-${String(startDateObj.getMonth() + 1).padStart(2, '0')}`;
  
  trendDateRange.value = [startDate, endDate];
  updateTrendChart();
};



// 窗口大小变化时重新调整图表
const handleResize = () => {
  if (departmentChart) departmentChart.resize();
  if (salaryChart) salaryChart.resize();
  if (trendChart) trendChart.resize();
};

// 组件挂载
onMounted(() => {
  setDefaultDateRange(); // 设置默认日期范围
  fetchStatistics();
  window.addEventListener('resize', handleResize);
});

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  if (departmentChart) {
    departmentChart.dispose();
    departmentChart = null;
  }
  if (salaryChart) {
    salaryChart.dispose();
    salaryChart = null;
  }
  if (trendChart) {
    trendChart.dispose();
    trendChart = null;
  }
});
</script>

<style lang="scss" scoped>
.salary-statistics {
  .stats-cards {
    margin-bottom: 24px;
    
    .stat-card {
      display: flex;
      align-items: center;
      padding: 20px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        
        .el-icon {
          font-size: 24px;
          color: #fff;
        }
        
        &.total {
          background: linear-gradient(135deg, #667eea, #764ba2);
        }
        
        &.average {
          background: linear-gradient(135deg, #f093fb, #f5576c);
        }
        
        &.highest {
          background: linear-gradient(135deg, #4facfe, #00f2fe);
        }
        
        &.employees {
          background: linear-gradient(135deg, #43e97b, #38f9d7);
        }
      }
      
      .stat-content {
        flex: 1;
        
        .stat-value {
          font-size: 1.8rem;
          font-weight: 700;
          color: #2c3e50;
          margin-bottom: 4px;
        }
        
        .stat-label {
          color: #7f8c8d;
          font-size: 0.9rem;
        }
      }
    }
  }
  
  .charts-section {
    margin-bottom: 24px;
    
    .chart-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .header-controls {
          display: flex;
          gap: 12px;
          align-items: center;
          flex-wrap: wrap;
          
          .filter-info {
            font-size: 12px;
            color: #409eff;
            background: #ecf5ff;
            padding: 4px 8px;
            border-radius: 4px;
            white-space: nowrap;
          }
          
          .quick-filters {
            display: flex;
            gap: 4px;
            align-items: center;
            
            .el-button {
              padding: 4px 8px;
              font-size: 11px;
              
              &:hover {
                background: #409eff;
                color: white;
              }
            }
          }
        }
      }
      
      .chart-container {
        height: 350px;
        width: 100%;
        min-height: 300px;
        
        &.trend-chart {
          height: 450px;
          min-height: 400px;
        }
      }
    }
  }
  
  .detailed-stats {
    // 表格样式优化
    .department-cell {
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 8px 0;
    }
    
    .count-cell {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 8px 0;
      
      .count-number {
        font-size: 18px;
        font-weight: 600;
        color: #2c3e50;
        line-height: 1.2;
      }
      
      .count-unit {
        font-size: 12px;
        color: #8492a6;
        margin-top: 2px;
      }
    }
    
    .salary-cell {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding: 8px 12px;
      gap: 8px;
      
      .salary-amount {
        font-weight: 600;
        font-size: 14px;
        
        &.total {
          color: #409eff;
          font-size: 16px;
        }
        
        &.average {
          color: #67c23a;
        }
        
        &.highest {
          color: #e6a23c;
          font-size: 15px;
        }
        
        &.lowest {
          color: #909399;
        }
      }
      
      .trend-icon {
        color: #e6a23c;
        font-size: 12px;
      }
    }
    
    .ratio-cell {
      padding: 12px;
      
      .ratio-desc {
        font-size: 11px;
        color: #8492a6;
        margin-top: 4px;
        text-align: center;
      }
    }
    
    // 进度条样式
    :deep(.el-progress) {
      .el-progress-bar__outer {
        background-color: #f0f2f5;
      }
      
      .el-progress__text {
        font-weight: 600;
        font-size: 12px;
      }
         }
     
     .chart-placeholder {
       display: flex;
       flex-direction: column;
       align-items: center;
       justify-content: center;
       height: 100%;
       color: #909399;
       
       .el-icon {
         font-size: 48px;
         margin-bottom: 12px;
       }
       
       p {
         margin: 0;
         font-size: 14px;
         color: #606266;
       }
     }
   }
 }
</style> 