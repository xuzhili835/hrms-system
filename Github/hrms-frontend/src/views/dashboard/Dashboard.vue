<template>
  <div class="dashboard">
    <!-- 顶部工具栏 -->
    <div class="dashboard-header">
      <h2 class="dashboard-title">控制台概览</h2>
      <div class="dashboard-actions">
        <el-button type="primary" icon="Refresh" @click="refreshData">刷新数据</el-button>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="default"
        />
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card employee-count">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <h3>员工总数</h3>
            <p class="stat-value">128</p>
            <div class="stat-trend">
              <span class="trend-up">↑ 8.5%</span> 较上月
            </div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card attendance">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <h3>今日考勤</h3>
            <p class="stat-value">98%</p>
            <div class="stat-trend">
              <span class="trend-up">↑ 2.1%</span> 较昨日
            </div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card salary">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <h3>本月薪资</h3>
            <p class="stat-value">¥386,420</p>
            <div class="stat-trend">
              <span class="trend-up">↑ 5.3%</span> 较上月
            </div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card pending">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <h3>待处理申请</h3>
            <p class="stat-value">12</p>
            <div class="stat-trend">
              <span class="trend-down">↓ 3</span> 较昨日
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-container">
      <div class="main-chart">
        <el-card>
          <div class="chart-header">
            <h3>月度人力资源分析</h3>
            <div class="chart-actions">
              <el-radio-group v-model="chartType" size="default">
                <el-radio-button value="line">折线图</el-radio-button>
                <el-radio-button value="bar">柱状图</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="chart-wrapper">
            <div ref="mainChart" style="height: 400px;"></div>
          </div>
        </el-card>
      </div>
      
      <div class="side-charts">
        <el-card class="chart-card">
          <h3>部门人员分布</h3>
          <div class="chart-wrapper">
            <div ref="departmentChart" style="height: 300px;"></div>
          </div>
        </el-card>
        
        <el-card class="chart-card">
          <h3>员工状态统计</h3>
          <div class="chart-wrapper">
            <div ref="statusChart" style="height: 300px;"></div>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 最近活动 -->
    <div class="recent-activity">
      <el-card>
        <h3>最近活动</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(activity, index) in activities"
            :key="index"
            :timestamp="activity.timestamp"
            placement="top"
          >
            <el-card>
              <h4>{{ activity.content }}</h4>
              <p class="activity-time">{{ activity.timestamp }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { User, Clock, Money, Document } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';

const router = useRouter();
const dateRange = ref([]);
const chartType = ref('line');

// 存储图表实例和事件处理函数
let mainChartInstance = null;
let departmentChartInstance = null;
let statusChartInstance = null;

const handleMainChartResize = () => {
  if (mainChartInstance) {
    mainChartInstance.resize();
  }
};

const handleDepartmentChartResize = () => {
  if (departmentChartInstance) {
    departmentChartInstance.resize();
  }
};

const handleStatusChartResize = () => {
  if (statusChartInstance) {
    statusChartInstance.resize();
  }
};

// 模拟最近活动数据
const activities = ref([
  { timestamp: '2023-08-15 14:20', content: '张三办理了入职手续' },
  { timestamp: '2023-08-14 10:30', content: '李四提交了请假申请' },
  { timestamp: '2023-08-13 16:45', content: '王五更新了个人信息' },
  { timestamp: '2023-08-12 09:15', content: '人力资源部发布了新的招聘计划' }
]);

// 初始化图表
onMounted(() => {
  initMainChart();
  initDepartmentChart();
  initStatusChart();
});

// 组件卸载时清理资源
onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('resize', handleMainChartResize);
  window.removeEventListener('resize', handleDepartmentChartResize);
  window.removeEventListener('resize', handleStatusChartResize);
  
  // 销毁图表实例
  if (mainChartInstance) {
    mainChartInstance.dispose();
    mainChartInstance = null;
  }
  if (departmentChartInstance) {
    departmentChartInstance.dispose();
    departmentChartInstance = null;
  }
  if (statusChartInstance) {
    statusChartInstance.dispose();
    statusChartInstance = null;
  }
});

const initMainChart = () => {
  const chartDom = document.querySelector('.main-chart .chart-wrapper > div');
  if (!chartDom) return;
  
  const chart = echarts.init(chartDom);
  chart.setOption({
    tooltip: { 
      trigger: 'axis',
      formatter: function(params) {
        let result = params[0].name + '<br/>';
        params.forEach(item => {
          result += `${item.marker} ${item.seriesName}: ${item.value}<br/>`;
        });
        return result;
      }
    },
    legend: { 
      data: ['入职人数', '离职人数', '在职人数'],
      bottom: 10
    },
    grid: { 
      left: '3%', 
      right: '4%', 
      bottom: '60px', 
      top: '30px',
      containLabel: true 
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月'],
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      }
    },
    yAxis: { 
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#dcdfe6'
        }
      },
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: [
      { 
        name: '入职人数', 
        type: 'bar', 
        data: [23, 18, 25, 28, 32, 30, 35],
        itemStyle: {
          color: '#36a3f7'
        }
      },
      { 
        name: '离职人数', 
        type: 'bar', 
        data: [5, 8, 6, 10, 7, 9, 8],
        itemStyle: {
          color: '#f4516c'
        }
      },
      { 
        name: '在职人数', 
        type: 'line', 
        data: [120, 125, 130, 135, 140, 142, 145],
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3
        },
        itemStyle: {
          color: '#34bfa3'
        }
      }
    ]
  });
  
  // 存储图表实例
  mainChartInstance = chart;
  
  // 响应窗口大小变化
  window.addEventListener('resize', handleMainChartResize);
};

const initDepartmentChart = () => {
  const chartDom = document.querySelector('.side-charts .chart-card:nth-child(1) .chart-wrapper > div');
  if (!chartDom) return;
  
  departmentChartInstance = echarts.init(chartDom);
  departmentChartInstance.setOption({
    tooltip: { 
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: { 
      orient: 'vertical', 
      right: 10, 
      top: 'center',
      formatter: function(name) {
        const data = departmentChartInstance.getOption().series[0].data;
        const item = data.find(d => d.name === name);
        return `${name}: ${item.value} (${item.percentage}%)`;
      }
    },
    series: [
      {
        name: '部门分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { 
          borderRadius: 6, 
          borderColor: '#fff', 
          borderWidth: 2 
        },
        label: { 
          show: false, 
          position: 'center' 
        },
        emphasis: { 
          label: { 
            show: true, 
            fontSize: '18', 
            fontWeight: 'bold' 
          } 
        },
        labelLine: { show: false },
        data: [
          { value: 35, name: '技术部', itemStyle: { color: '#36a3f7' } },
          { value: 28, name: '市场部', itemStyle: { color: '#34bfa3' } },
          { value: 20, name: '销售部', itemStyle: { color: '#ffb822' } },
          { value: 18, name: '人力资源', itemStyle: { color: '#716aca' } },
          { value: 15, name: '财务部', itemStyle: { color: '#f4516c' } },
          { value: 12, name: '行政部', itemStyle: { color: '#8d6e63' } }
        ]
      }
    ]
  });
  
  window.addEventListener('resize', handleDepartmentChartResize);
};

const initStatusChart = () => {
  const chartDom = document.querySelector('.side-charts .chart-card:nth-child(2) .chart-wrapper > div');
  if (!chartDom) return;
  
  statusChartInstance = echarts.init(chartDom);
  statusChartInstance.setOption({
    tooltip: { 
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: { 
      orient: 'vertical', 
      right: 10, 
      top: 'center',
      formatter: function(name) {
        const data = statusChartInstance.getOption().series[0].data;
        const item = data.find(d => d.name === name);
        return `${name}: ${item.value} (${item.percentage}%)`;
      }
    },
    series: [
      {
        name: '员工状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { 
          borderRadius: 6, 
          borderColor: '#fff', 
          borderWidth: 2 
        },
        label: { 
          show: false, 
          position: 'center' 
        },
        emphasis: { 
          label: { 
            show: true, 
            fontSize: '18', 
            fontWeight: 'bold' 
          } 
        },
        labelLine: { show: false },
        data: [
          { value: 120, name: '在职', itemStyle: { color: '#34bfa3' } },
          { value: 8, name: '试用期', itemStyle: { color: '#ffb822' } },
          { value: 5, name: '离职', itemStyle: { color: '#f4516c' } }
        ]
      }
    ]
  });
  
  window.addEventListener('resize', handleStatusChartResize);
};

// 刷新数据
const refreshData = () => {
  ElMessage.success('数据已刷新');
};
</script>

<style scoped>
.dashboard {
  width: 100%;
  max-width: 1800px;
  margin: 0 auto;
  padding: 8px 32px 32px 32px;
  box-sizing: border-box;
  min-height: calc(100vh - 64px);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.dashboard-title {
  font-size: 1.8rem;
  font-weight: 600;
  color: #1f2d3d;
  margin: 0;
}

.dashboard-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  color: white;
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}

.stat-card.employee-count {
  background: linear-gradient(135deg, #409EFF, #1e88e5);
}

.stat-card.attendance {
  background: linear-gradient(135deg, #67C23A, #43a047);
}

.stat-card.salary {
  background: linear-gradient(135deg, #E6A23C, #f57c00);
}

.stat-card.pending {
  background: linear-gradient(135deg, #F56C6C, #e53935);
}

.stat-content {
  display: flex;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  flex-shrink: 0;
}

.stat-icon .el-icon {
  font-size: 32px;
}

.stat-info h3 {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  opacity: 0.9;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin: 8px 0;
}

.stat-trend {
  font-size: 14px;
  opacity: 0.9;
  display: flex;
  align-items: center;
}

.trend-up {
  color: #aaffaa;
  font-weight: bold;
  margin-right: 5px;
}

.trend-down {
  color: #ffaaaa;
  font-weight: bold;
  margin-right: 5px;
}

.charts-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.main-chart {
  grid-column: 1;
}

.side-charts {
  grid-column: 2;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-wrapper {
  height: 400px;
}

.chart-card .chart-wrapper {
  height: 300px;
}

.recent-activity {
  margin-top: 24px;
}

.activity-time {
  color: #999;
  font-size: 0.9rem;
  margin-top: 5px;
}

@media (max-width: 1200px) {
  .dashboard {
    padding: 8px 24px 24px 24px;
  }
  
  .charts-container {
    grid-template-columns: 1fr;
  }
  
  .side-charts {
    grid-column: 1;
    flex-direction: row;
  }
  
  .side-charts .chart-card {
    flex: 1;
  }
}

@media (max-width: 992px) {
  .dashboard {
    padding: 8px 20px 20px 20px;
  }
  
  .side-charts {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 8px 16px 16px 16px;
  }
  
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .dashboard-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

/* 针对高分辨率屏幕的优化 */
@media (min-width: 2000px) {
  .dashboard {
    max-width: 2200px;
    padding: 32px 48px;
  }
  
  .dashboard-title {
    font-size: 2rem;
  }
  
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
  }
}
</style>