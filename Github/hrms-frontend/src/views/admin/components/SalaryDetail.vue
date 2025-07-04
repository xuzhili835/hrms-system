<template>
  <div class="salary-detail">
    <!-- 员工基本信息 -->
    <el-card class="employee-info" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>员工信息</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="info-item">
            <span class="label">工号：</span>
            <span class="value">{{ salaryData.empId || '未知' }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <span class="label">部门：</span>
            <span class="value">{{ employeeInfo?.department || '未知' }}</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="info-item">
            <span class="label">薪资月份：</span>
            <span class="value">{{ salaryData.month || '未知' }}</span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="info-item">
            <span class="label">职位：</span>
            <span class="value">{{ employeeInfo?.position || '未知' }}</span>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="20" v-if="salaryData.createdAt || salaryData.updatedAt">
        <el-col :span="12">
          <div class="info-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ formatDate(salaryData.createdAt) || '未知' }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <span class="label">更新时间：</span>
            <span class="value">{{ formatDate(salaryData.updatedAt) || '未知' }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 薪资构成 -->
    <el-card class="salary-breakdown" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Money /></el-icon>
          <span>薪资构成</span>
        </div>
      </template>
      
      <!-- 收入项目 -->
      <div class="section">
        <h4 class="section-title income">收入项目</h4>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="salary-item">
              <span class="label">基本工资</span>
              <span class="value income">¥{{ formatCurrency(salaryData.baseSalary) }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="salary-item">
              <span class="label">津贴</span>
              <span class="value income">¥{{ formatCurrency(salaryData.allowance) }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="salary-item">
              <span class="label">奖金</span>
              <span class="value income">¥{{ formatCurrency(salaryData.bonus) }}</span>
            </div>
          </el-col>
        </el-row>
        <div class="subtotal">
          <span class="label">应发工资小计：</span>
          <span class="value gross">¥{{ formatCurrency(calculateGrossSalary()) }}</span>
        </div>
      </div>

      <!-- 扣除项目 -->
      <div class="section">
        <h4 class="section-title deduction">扣除项目</h4>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="salary-item">
              <span class="label">总扣除</span>
              <span class="value deduction">¥{{ formatCurrency(salaryData.deduction) }}</span>
            </div>
          </el-col>
        </el-row>
        <div class="subtotal">
          <span class="label">扣除合计：</span>
          <span class="value deduction-total">¥{{ formatCurrency(salaryData.deduction) }}</span>
        </div>
      </div>

      <!-- 实发工资 -->
      <div class="net-salary">
        <div class="net-salary-item">
          <span class="label">总薪资：</span>
          <span class="value">¥{{ formatCurrency(salaryData.totalSalary) }}</span>
        </div>
      </div>
    </el-card>

    <!-- 薪资记录ID信息 -->
    <el-card class="record-info" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>记录信息</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="info-item">
            <span class="label">记录ID：</span>
            <span class="value">{{ salaryData.id || '未知' }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ formatDate(salaryData.createdAt) || '未知' }}</span>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 操作记录 -->
    <el-card class="operation-log" shadow="never" v-if="salaryData.operationLog && salaryData.operationLog.length > 0">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>操作记录</span>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(log, index) in salaryData.operationLog"
          :key="index"
          :timestamp="formatDate(log.timestamp)"
          :type="getLogType(log.action)"
        >
          <div class="log-content">
            <div class="log-action">{{ log.action }}</div>
            <div class="log-operator">操作人：{{ log.operator }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>


  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { User, Money, Document } from '@element-plus/icons-vue';
import { getEmployeeList } from '@/api/employee';

// Props
const props = defineProps({
  salaryData: {
    type: Object,
    required: true,
    default: () => ({})
  }
});

// 员工信息
const employeeInfo = ref({
  department: '',
  position: ''
});

// 获取员工信息
const fetchEmployeeInfo = async () => {
  try {
    if (!props.salaryData?.empId) {
      console.warn('❌ 没有员工ID，无法获取员工信息');
      return;
    }
    
    console.log('🔍 获取员工信息:', props.salaryData.empId);
    
    const response = await getEmployeeList();
    let employees = [];
    
    if (Array.isArray(response)) {
      employees = response;
    } else if (response.data && Array.isArray(response.data)) {
      employees = response.data;
    }
    
    const employee = employees.find(emp => emp.empId === props.salaryData.empId);
    if (employee) {
      // 更新员工信息
      employeeInfo.value = {
        department: employee.dept || '未知部门',
        position: employee.pos || '未知职位'
      };
      console.log('✅ 找到员工信息:', employeeInfo.value);
    } else {
      console.warn(`⚠️ 未找到员工信息: ${props.salaryData.empId}`);
      
      // 设置默认值
      employeeInfo.value = {
        department: '未知部门',
        position: '未知职位'
      };
    }
  } catch (error) {
    console.error('❌ 获取员工信息失败:', error);
    
    // 设置默认值以避免显示空白
    employeeInfo.value = {
      department: '未知部门',
      position: '未知职位'
    };
  }
};

// 格式化货币
const formatCurrency = (value) => {
  if (value == null || value === '') return '0';
  const num = Number(value);
  return isNaN(num) ? '0' : num.toLocaleString();
};

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    '草稿': 'info',
    '已确认': 'warning',
    '已发放': 'success',
    '已取消': 'danger'
  };
  return statusMap[status] || 'info';
};

// 获取状态文本
const getStatusText = (status) => {
  // 数据库中存储的就是中文状态，直接返回
  return status || '草稿';
};

// 获取日志类型
const getLogType = (action) => {
  const typeMap = {
    '创建': 'primary',
    '编辑': 'warning',
    '确认': 'success',
    '发放': 'success',
    '取消': 'danger'
  };
  return typeMap[action] || 'primary';
};

// 格式化日期
const formatDate = (date) => {
  if (!date) return '';
  try {
    return new Date(date).toLocaleString('zh-CN');
  } catch (error) {
    return '无效日期';
  }
};

// 计算应发工资小计
const calculateGrossSalary = () => {
  const base = Number(props.salaryData.baseSalary) || 0;
  const allowance = Number(props.salaryData.allowance) || 0;
  const bonus = Number(props.salaryData.bonus) || 0;
  return base + allowance + bonus;
};

// 监听数据变化
watch(() => props.salaryData, (newData) => {
  if (newData && newData.empId) {
    console.log('📋 薪资数据更新，重新获取员工信息:', newData);
    fetchEmployeeInfo();
  }
}, { immediate: true, deep: true });

// 页面挂载时获取员工信息
onMounted(() => {
  fetchEmployeeInfo();
});
</script>

<style lang="scss" scoped>
.salary-detail {
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: #409eff;
  }
  
  .employee-info {
    margin-bottom: 20px;
    
    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      
      .label {
        color: #7f8c8d;
        font-weight: 500;
        min-width: 60px;
      }
      
      .value {
        color: #2c3e50;
        font-weight: 600;
      }
    }
  }
  
  .salary-breakdown, .record-info {
    margin-bottom: 20px;
    
    .section {
      margin-bottom: 24px;
      
      .section-title {
        font-size: 1.1rem;
        font-weight: 600;
        margin-bottom: 16px;
        padding-bottom: 8px;
        border-bottom: 2px solid #ebeef5;
        
        &.income {
          color: #67c23a;
          border-bottom-color: #67c23a;
        }
        
        &.deduction {
          color: #f56c6c;
          border-bottom-color: #f56c6c;
        }
      }
      
      .salary-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 16px;
        background: #f8f9fa;
        border-radius: 8px;
        margin-bottom: 12px;
        
        .label {
          color: #7f8c8d;
          font-size: 0.9rem;
          margin-bottom: 8px;
        }
        
        .value {
          font-weight: 700;
          font-size: 1.1rem;
          
          &.income {
            color: #67c23a;
          }
          
          &.deduction {
            color: #f56c6c;
          }
        }
      }
      
      .subtotal {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        background: #f0f2f5;
        border-radius: 8px;
        margin-top: 16px;
        
        .label {
          font-weight: 600;
          color: #2c3e50;
        }
        
        .value {
          font-weight: 700;
          font-size: 1.2rem;
          
          &.gross {
            color: #409eff;
          }
          
          &.deduction-total {
            color: #f56c6c;
          }
        }
      }
    }
    
    .net-salary {
      padding: 20px;
      background: linear-gradient(135deg, #67c23a, #85ce61);
      border-radius: 12px;
      text-align: center;
      
      .net-salary-item {
        .label {
          color: #fff;
          font-size: 1.2rem;
          font-weight: 600;
          margin-right: 12px;
        }
        
        .value {
          color: #fff;
          font-size: 2rem;
          font-weight: 700;
          text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
        }
      }
    }
  }
  
  .operation-log {
    margin-bottom: 20px;
    
    .log-content {
      .log-action {
        font-weight: 600;
        color: #2c3e50;
        margin-bottom: 4px;
      }
      
      .log-operator {
        color: #7f8c8d;
        font-size: 0.9rem;
        margin-bottom: 4px;
      }
      

    }
  }

}
</style> 