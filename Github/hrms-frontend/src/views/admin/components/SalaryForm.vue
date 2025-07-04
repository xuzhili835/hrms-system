<template>
  <div class="salary-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      @submit.prevent="handleSubmit"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="员工工号" prop="empId">
            <el-input
              v-model="form.empId"
              placeholder="请输入员工工号"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="月份" prop="month">
            <el-date-picker
              v-model="form.month"
              type="month"
              placeholder="选择月份"
              format="YYYY-MM"
              value-format="YYYY-MM"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="基本工资" prop="baseSalary">
            <el-input-number
              v-model="form.baseSalary"
              :min="0"
              :max="100000"
              :step="100"
              style="width: 100%"
              @change="calculateTotal"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="津贴" prop="allowance">
            <el-input-number
              v-model="form.allowance"
              :min="0"
              :max="20000"
              :step="100"
              style="width: 100%"
              @change="calculateTotal"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="奖金" prop="bonus">
            <el-input-number
              v-model="form.bonus"
              :min="0"
              :max="50000"
              :step="100"
              style="width: 100%"
              @change="calculateTotal"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="扣除" prop="deduction">
            <el-input-number
              v-model="form.deduction"
              :min="0"
              :max="10000"
              :step="50"
              style="width: 100%"
              @change="calculateTotal"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="总薪资" prop="totalSalary">
            <el-input-number
              v-model="form.totalSalary"
              :min="0"
              :max="200000"
              :step="100"
              style="width: 100%"
              readonly
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
              <el-option label="草稿" value="草稿" />
              <el-option label="已确认" value="已确认" />
              <el-option label="已发放" value="已发放" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 薪资汇总 -->
      <el-card class="salary-summary" shadow="never">
        <template #header>
          <span>薪资汇总</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="summary-item">
              <span class="label">应发工资：</span>
              <span class="value gross">¥{{ grossSalary.toLocaleString() }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="summary-item">
              <span class="label">扣除合计：</span>
              <span class="value deduction">¥{{ (form.deduction || 0).toLocaleString() }}</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="summary-item">
              <span class="label">总薪资：</span>
              <span class="value net">¥{{ (form.totalSalary || 0).toLocaleString() }}</span>
            </div>
          </el-col>
        </el-row>
      </el-card>


    </el-form>

    <div class="form-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ isEdit ? '更新' : '创建' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';

// Props
const props = defineProps({
  formData: {
    type: Object,
    default: () => ({})
  },
  isEdit: {
    type: Boolean,
    default: false
  }
});

// Emits
const emit = defineEmits(['submit', 'cancel']);

// 响应式数据
const formRef = ref();
const submitting = ref(false);

// 表单数据
const form = reactive({
  empId: '',
  month: '',
  baseSalary: 0,
  allowance: 0,
  bonus: 0,
  deduction: 0,
  totalSalary: 0,
  status: '草稿'
});

// 表单验证规则
const rules = reactive({
  empId: [
    { required: true, message: '请输入员工工号', trigger: 'blur' }
  ],
  month: [
    { required: true, message: '请选择月份', trigger: 'change' }
  ],
  baseSalary: [
    { required: true, message: '请输入基本工资', trigger: 'blur' },
    { type: 'number', min: 0, message: '基本工资不能为负数', trigger: 'blur' }
  ]
});

// 计算属性
const grossSalary = computed(() => {
  return form.baseSalary + form.bonus + form.allowance;
});

const netSalary = computed(() => {
  return grossSalary.value - (form.deduction || 0);
});



// 监听表单数据变化
watch(() => props.formData, (newData) => {
  if (newData && Object.keys(newData).length > 0) {
    // 添加深度检查，避免不必要的赋值
    let hasChanged = false;
    Object.keys(newData).forEach(key => {
      if (form[key] !== newData[key]) {
        hasChanged = true;
      }
    });
    
    if (hasChanged) {
      Object.assign(form, newData);
    }
  }
}, { deep: true });

// 监听薪资相关字段变化，自动计算总薪资
watch([() => form.baseSalary, () => form.allowance, () => form.bonus, () => form.deduction], () => {
  calculateTotal();
}, { immediate: true });

// 优化计算总额的方法
let totalCalculationTimer = null;
const calculateTotal = () => {
  if (totalCalculationTimer) {
    clearTimeout(totalCalculationTimer);
  }
  
  totalCalculationTimer = setTimeout(() => {
    // 计算总薪资
    form.totalSalary = form.baseSalary + form.bonus + form.allowance - (form.deduction || 0);
  }, 100);
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    submitting.value = true;
    
    // 只发送数据库表中存在的字段
    const formData = {
      ...form
    };
    
    emit('submit', formData);
  } catch (error) {
    ElMessage.error('表单验证失败');
  } finally {
    submitting.value = false;
  }
};

// 取消操作
const handleCancel = () => {
  emit('cancel');
};

// 组件卸载时清理定时器
onUnmounted(() => {
  if (totalCalculationTimer) {
    clearTimeout(totalCalculationTimer);
  }
});


</script>

<style lang="scss" scoped>
.salary-form {
  .salary-summary {
    margin: 20px 0;
    
    .summary-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      
      .label {
        color: #7f8c8d;
        font-weight: 500;
      }
      
      .value {
        font-weight: 700;
        font-size: 1.1em;
        
        &.gross {
          color: #409eff;
        }
        
        &.deduction {
          color: #f56c6c;
        }
        
        &.net {
          color: #67c23a;
          font-size: 1.2em;
        }
      }
    }
  }
  
  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
  }
}
</style> 