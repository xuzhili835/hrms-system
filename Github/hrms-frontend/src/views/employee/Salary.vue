<template>
  <div class="salary-page">
    <div class="page-header">
      <h1>薪资查看</h1>
      <p>查看您的历史薪资记录和详细信息</p>
    </div>

    <!-- 薪资概览卡片 -->
    <div class="salary-overview">
      <el-card class="overview-card current-salary">
        <template #header>
          <div class="card-header">
            <span>本月薪资</span>
            <el-tag type="success">{{ currentSalary.status || '已发放' }}</el-tag>
          </div>
        </template>
        <div class="salary-amount">
          <span class="amount">¥{{ formatNumber(currentSalary.totalSalary) }}</span>
          <span class="label">总薪资</span>
        </div>
        <div class="salary-breakdown">
          <div class="breakdown-item">
            <span>基本工资：¥{{ formatNumber(currentSalary.baseSalary) }}</span>
          </div>
          <div class="breakdown-item">
            <span>津贴：¥{{ formatNumber(currentSalary.allowance) }}</span>
          </div>
          <div class="breakdown-item">
            <span>奖金：¥{{ formatNumber(currentSalary.bonus) }}</span>
          </div>
          <div class="breakdown-item">
            <span>扣除：¥{{ formatNumber(currentSalary.deduction) }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card year-summary">
        <template #header>
          <span>年度汇总</span>
        </template>
        <div class="summary-stats">
          <div class="stat-item">
            <div class="stat-value">{{ salaryHistory.length }}</div>
            <div class="stat-label">发薪月数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">¥{{ formatNumber(yearlyTotal) }}</div>
            <div class="stat-label">年度总收入</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 薪资历史记录 -->
    <el-card class="history-card">
      <template #header>
        <div class="history-header">
          <span>薪资历史记录</span>
          <el-button 
            type="primary" 
            @click="refreshSalaryData"
            :loading="loading"
            size="small"
          >
            刷新数据
          </el-button>
        </div>
      </template>

      <el-table 
        :data="paginatedSalaryHistory" 
        v-loading="loading"
        stripe
        style="width: 100%"
        size="large"
        class="salary-table"
      >
        <el-table-column prop="month" label="月份" min-width="140" show-overflow-tooltip>
          <template #default="scope">
            <span class="month-text">{{ scope.row.month }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="baseSalary" label="基本工资" min-width="150" show-overflow-tooltip>
          <template #default="scope">
            <span class="salary-amount">¥{{ formatNumber(scope.row.baseSalary) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="allowance" label="津贴" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <span class="salary-amount">¥{{ formatNumber(scope.row.allowance) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="bonus" label="奖金" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <span class="salary-amount bonus">¥{{ formatNumber(scope.row.bonus) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="deduction" label="扣除" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <span class="salary-amount deduction">¥{{ formatNumber(scope.row.deduction) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="totalSalary" label="总薪资" min-width="160" show-overflow-tooltip>
          <template #default="scope">
            <span class="total-salary-cell">¥{{ formatNumber(scope.row.totalSalary) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" min-width="100" show-overflow-tooltip>
          <template #default="scope">
            <el-tag 
              :type="scope.row.status === '已发放' ? 'success' : 'warning'"
              size="default"
              effect="light"
            >
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" min-width="140" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              size="default"
              @click="viewSalaryDetail(scope.row)"
              class="detail-btn"
            >
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="salaryHistory.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="salary-pagination"
        />
      </div>
    </el-card>

    <!-- 薪资详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="薪资详情" 
      width="600px"
      center
    >
      <div class="salary-detail" v-if="selectedSalary">
        <div class="detail-header">
          <h3>{{ selectedSalary.month }} 薪资详情</h3>
        </div>
        
        <div class="detail-content">
          <div class="detail-section">
            <h4>收入明细</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">基本工资：</span>
                <span class="value">¥{{ formatNumber(selectedSalary.baseSalary) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">津贴：</span>
                <span class="value">¥{{ formatNumber(selectedSalary.allowance) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">奖金：</span>
                <span class="value">¥{{ formatNumber(selectedSalary.bonus) }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>扣除明细</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">总扣除：</span>
                <span class="value deduction">¥{{ formatNumber(selectedSalary.deduction) }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section total-section">
            <div class="total-item">
              <span class="label">总薪资：</span>
              <span class="value total">¥{{ formatNumber(selectedSalary.totalSalary) }}</span>
            </div>
            <div class="total-item">
              <span class="label">实发薪资：</span>
              <span class="value actual">¥{{ formatNumber(calculateActualSalary(selectedSalary)) }}</span>
            </div>
            <div class="total-item">
              <span class="label">发放状态：</span>
              <el-tag 
                :type="selectedSalary.status === '已发放' ? 'success' : 'warning'"
                size="small"
              >
                {{ selectedSalary.status }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { View } from '@element-plus/icons-vue';
import { getEmployeeSalaryHistory, getEmployeeYearlyIncome } from '@/api/salary';

// 响应式数据
const loading = ref(false);
const salaryHistory = ref([]);
const detailDialogVisible = ref(false);
const selectedSalary = ref(null);
const actualYearlyTotal = ref(0); // 从API获取的实际年度总收入
const currentPage = ref(1); // 当前页码
const pageSize = ref(10); // 每页显示数量

// 计算属性
const currentSalary = computed(() => {
  if (salaryHistory.value.length === 0) return {
    baseSalary: 0,
    allowance: 0,
    bonus: 0,
    deduction: 0,
    totalSalary: 0,
    status: '未发放'
  };
  
  // 获取最新月份的薪资
  const currentMonth = new Date().getMonth() + 1;
  const currentYear = new Date().getFullYear();
  const currentMonthStr = `${currentYear}-${String(currentMonth).padStart(2, '0')}`;
  
  const current = salaryHistory.value.find(item => item.month === currentMonthStr);
  return current || salaryHistory.value[0] || {};
});

const yearlyTotal = computed(() => {
  // 优先使用API获取的年度总收入
  if (actualYearlyTotal.value > 0) {
    console.log('💯 使用API获取的年度总收入:', actualYearlyTotal.value);
    return actualYearlyTotal.value;
  }
  
  // 备用方案：从历史记录计算
  const currentYear = new Date().getFullYear();
  console.log('📊 计算年度总收入:', {
    currentYear,
    salaryHistory: salaryHistory.value,
    filteredData: salaryHistory.value.filter(item => item.month && item.month.startsWith(String(currentYear)))
  });
  
  const total = salaryHistory.value
    .filter(item => {
      // 确保month字段存在且格式正确
      const month = item.month;
      if (!month) return false;
      
      // 支持多种日期格式
      const isCurrentYear = month.startsWith(String(currentYear));
      console.log('📅 薪资记录筛选:', { 
        month, 
        isCurrentYear, 
        totalSalary: item.totalSalary,
        totalSalaryNumber: Number(item.totalSalary || 0)
      });
      return isCurrentYear;
    })
    .reduce((total, item) => {
      // 确保数值转换正确
      const salary = parseFloat(item.totalSalary) || 0;
      console.log('💰 累加薪资:', { 
        month: item.month, 
        originalSalary: item.totalSalary,
        parsedSalary: salary, 
        currentTotal: total,
        newTotal: total + salary
      });
      return total + salary;
    }, 0);
    
  console.log('💯 年度总收入计算结果:', total);
  return total;
});

// 分页后的薪资历史记录
const paginatedSalaryHistory = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return salaryHistory.value.slice(start, end);
});

// 方法
const formatNumber = (value) => {
  const num = Number(value || 0);
  return num.toLocaleString();
};

const calculateActualSalary = (salary) => {
  const total = Number(salary.totalSalary || 0);
  const deduction = Number(salary.deduction || 0);
  return total - deduction;
};

const fetchSalaryHistory = async () => {
  try {
    loading.value = true;
    console.log('🔄 获取薪资历史记录');
    
    // 获取当前用户信息
    const userEmployeeId = localStorage.getItem('user_employee_id');
    if (!userEmployeeId) {
      console.warn('⚠️ 无法获取用户员工ID');
      ElMessage.warning('无法获取用户信息，请重新登录');
      return;
    }
    
    console.log('📋 员工ID:', userEmployeeId);
    
    // 并行获取薪资历史记录和年度总收入
    const currentYear = new Date().getFullYear();
    const [historyResponse, yearlyResponse] = await Promise.allSettled([
      getEmployeeSalaryHistory(userEmployeeId),
      getEmployeeYearlyIncome(userEmployeeId, currentYear.toString())
    ]);
    
    // 处理薪资历史记录
    if (historyResponse.status === 'fulfilled') {
      const response = historyResponse.value;
      console.log('📊 薪资数据获取成功:', response);
      
      // 处理响应数据
      if (response && response.data && Array.isArray(response.data)) {
        salaryHistory.value = response.data.map(item => ({
          ...item,
          baseSalary: Number(item.baseSalary || 0),
          allowance: Number(item.allowance || 0),
          bonus: Number(item.bonus || 0),
          deduction: Number(item.deduction || 0),
          totalSalary: Number(item.totalSalary || 0),
          status: item.status || '已发放'
        }));
        
        console.log('✅ 薪资数据处理完成:', salaryHistory.value);
        
        if (salaryHistory.value.length === 0) {
          ElMessage.info('暂无薪资记录');
        }
      } else if (response && Array.isArray(response)) {
        // 直接返回数组的情况
        salaryHistory.value = response.map(item => ({
          ...item,
          baseSalary: Number(item.baseSalary || 0),
          allowance: Number(item.allowance || 0),
          bonus: Number(item.bonus || 0),
          deduction: Number(item.deduction || 0),
          totalSalary: Number(item.totalSalary || 0),
          status: item.status || '已发放'
        }));
      } else {
        console.warn('⚠️ 薪资数据格式异常:', response);
        salaryHistory.value = [];
        ElMessage.warning('薪资数据格式异常');
      }
    } else {
      console.error('❌ 获取薪资历史记录失败:', historyResponse.reason);
      salaryHistory.value = [];
    }
    
    // 处理年度总收入
    if (yearlyResponse.status === 'fulfilled') {
      const yearlyData = yearlyResponse.value;
      console.log('💰 年度总收入API响应:', yearlyData);
      
      // 尝试多种可能的响应格式
      let totalIncome = 0;
      
      if (yearlyData && yearlyData.data && yearlyData.data.totalIncome !== undefined) {
        // 格式1: { data: { totalIncome: 129840 } }
        totalIncome = Number(yearlyData.data.totalIncome);
      } else if (yearlyData && yearlyData.totalIncome !== undefined) {
        // 格式2: { totalIncome: 129840 }
        totalIncome = Number(yearlyData.totalIncome);
      } else if (yearlyData && typeof yearlyData === 'number') {
        // 格式3: 直接返回数字
        totalIncome = yearlyData;
      } else if (yearlyData && yearlyData.data && typeof yearlyData.data === 'number') {
        // 格式4: { data: 129840 }
        totalIncome = Number(yearlyData.data);
      } else {
        console.warn('⚠️ 未识别的年度总收入API响应格式:', yearlyData);
      }
      
      actualYearlyTotal.value = totalIncome;
      console.log('💯 年度总收入设置为:', actualYearlyTotal.value);
    } else {
      console.warn('⚠️ 获取年度总收入失败，将使用历史记录计算:', yearlyResponse.reason);
      actualYearlyTotal.value = 0; // 重置为0，让计算属性使用历史记录计算
    }
    
  } catch (error) {
    console.error('❌ 获取薪资数据失败:', error);
    ElMessage.error('获取薪资数据失败：' + (error.message || '未知错误'));
    salaryHistory.value = [];
    actualYearlyTotal.value = 0;
  } finally {
    loading.value = false;
  }
};

const refreshSalaryData = () => {
  fetchSalaryHistory();
};

const viewSalaryDetail = (salary) => {
  selectedSalary.value = salary;
  detailDialogVisible.value = true;
};

// 分页方法
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 1; // 重置到第一页
};

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage;
};

// 页面挂载时获取数据
onMounted(() => {
  fetchSalaryHistory();
});
</script>

<style scoped>
.salary-page {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 2rem;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.page-header p {
  color: #7f8c8d;
  margin: 0;
}

.salary-overview {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.overview-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.current-salary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.current-salary :deep(.el-card__header) {
  background: transparent;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.current-salary :deep(.el-card__body) {
  background: transparent;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.salary-amount {
  text-align: center;
  margin-bottom: 24px;
}

.salary-amount .amount {
  display: block;
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 8px;
}

.salary-amount .label {
  font-size: 1rem;
  opacity: 0.8;
}

.salary-breakdown {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.breakdown-item {
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  text-align: center;
  font-size: 0.9rem;
}

.year-summary .summary-stats {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 8px;
}

.stat-label {
  color: #7f8c8d;
  font-size: 0.9rem;
}

.history-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 薪资表格样式优化 */
.salary-table {
  border-radius: 8px;
  overflow: hidden;
}

.salary-table :deep(.el-table__header) {
  background-color: #f8f9fa;
}

.salary-table :deep(.el-table__header th) {
  background-color: #f8f9fa;
  color: #2c3e50;
  font-weight: 600;
  border-bottom: 2px solid #e9ecef;
  padding: 16px 12px;
}

.salary-table :deep(.el-table__body tr) {
  height: 60px;
}

.salary-table :deep(.el-table__body td) {
  padding: 12px;
  vertical-align: middle;
}

.salary-table :deep(.el-table__body tr:hover > td) {
  background-color: #f8f9fa !important;
}

/* 表格内容样式 */
.month-text {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.salary-amount {
  font-weight: 500;
  color: #2c3e50;
  font-size: 14px;
}

.salary-amount.bonus {
  color: #67c23a;
  font-weight: 600;
}

.salary-amount.deduction {
  color: #f56c6c;
  font-weight: 600;
}

.total-salary-cell {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
  background: linear-gradient(45deg, #409eff, #67c23a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.detail-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s;
}

.detail-btn:hover {
  background-color: #ecf5ff;
  transform: translateY(-1px);
}

.total-salary {
  font-weight: bold;
  color: #67c23a;
}

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 0;
  border-top: 1px solid #ebeef5;
}

.salary-pagination {
  background: transparent;
}

.salary-pagination :deep(.el-pagination__total) {
  color: #606266;
  font-weight: 500;
}

.salary-pagination :deep(.el-pagination__sizes) {
  color: #606266;
}

.salary-pagination :deep(.el-select .el-input__inner) {
  border-color: #dcdfe6;
}

.salary-pagination :deep(.btn-prev),
.salary-pagination :deep(.btn-next) {
  background-color: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.salary-pagination :deep(.btn-prev:hover),
.salary-pagination :deep(.btn-next:hover) {
  background-color: #409eff;
  border-color: #409eff;
  color: #fff;
}

.salary-detail .detail-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.detail-item .label {
  color: #7f8c8d;
}

.detail-item .value {
  font-weight: bold;
  color: #2c3e50;
}

.detail-item .value.deduction {
  color: #f56c6c;
}

.total-section {
  border-top: 2px solid #ebeef5;
  padding-top: 20px;
}

.total-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  font-size: 1.1rem;
}

.total-item .label {
  color: #2c3e50;
  font-weight: 600;
}

.total-item .value.total {
  color: #409eff;
  font-weight: bold;
  font-size: 1.2rem;
}

.total-item .value.actual {
  color: #67c23a;
  font-weight: bold;
  font-size: 1.3rem;
}

@media (max-width: 768px) {
  .salary-overview {
    grid-template-columns: 1fr;
  }
  
  .salary-breakdown {
    grid-template-columns: 1fr;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  /* 移动端表格优化 */
  .salary-table :deep(.el-table__header th) {
    padding: 12px 8px;
    font-size: 12px;
  }
  
  .salary-table :deep(.el-table__body td) {
    padding: 8px;
  }
  
  .salary-table :deep(.el-table__body tr) {
    height: 50px;
  }
  
  .month-text,
  .salary-amount {
    font-size: 12px;
  }
  
  .total-salary-cell {
    font-size: 14px;
  }
  
  .detail-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .salary-page {
    padding: 16px;
  }
  
  .page-header h1 {
    font-size: 1.5rem;
  }
  
  /* 超小屏幕时隐藏部分列，保持核心信息显示 */
  .salary-table :deep(.el-table__header th:nth-child(3)),
  .salary-table :deep(.el-table__header th:nth-child(4)),
  .salary-table :deep(.el-table__body td:nth-child(3)),
  .salary-table :deep(.el-table__body td:nth-child(4)) {
    display: none;
  }
}
</style> 