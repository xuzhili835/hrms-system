<template>
  <div class="salary-standards">
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        添加薪资标准
      </el-button>
      <el-button @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 薪资标准表格 -->
    <el-table 
      :data="standardsList" 
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column prop="position" label="职位" width="150" />
      <el-table-column prop="department" label="部门" width="120" />
      <el-table-column prop="level" label="级别" width="100" />
      <el-table-column prop="baseSalary" label="基本工资" width="120">
        <template #default="{ row }">
          <span class="salary-amount">¥{{ row.baseSalary.toLocaleString() }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="performanceBonus" label="绩效奖金" width="120">
        <template #default="{ row }">
          <span class="salary-amount">¥{{ row.performanceBonus.toLocaleString() }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="allowance" label="津贴补助" width="120">
        <template #default="{ row }">
          <span class="salary-amount">¥{{ row.allowance.toLocaleString() }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalSalary" label="总薪资" width="120">
        <template #default="{ row }">
          <span class="salary-amount total">¥{{ row.totalSalary.toLocaleString() }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'info'">
            {{ row.status === 'active' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.updateTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editStandard(row)">编辑</el-button>
          <el-button 
            size="small" 
            :type="row.status === 'active' ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 'active' ? '停用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteStandard(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑薪资标准对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingStandard ? '编辑薪资标准' : '添加薪资标准'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="form.position" placeholder="请输入职位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="department">
              <el-select v-model="form.department" placeholder="请选择部门" style="width: 100%">
                <el-option label="技术部" value="技术部" />
                <el-option label="产品部" value="产品部" />
                <el-option label="市场部" value="市场部" />
                <el-option label="销售部" value="销售部" />
                <el-option label="人事部" value="人事部" />
                <el-option label="财务部" value="财务部" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="级别" prop="level">
              <el-select v-model="form.level" placeholder="请选择级别" style="width: 100%">
                <el-option label="初级" value="初级" />
                <el-option label="中级" value="中级" />
                <el-option label="高级" value="高级" />
                <el-option label="专家" value="专家" />
                <el-option label="主管" value="主管" />
                <el-option label="经理" value="经理" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="基本工资" prop="baseSalary">
              <el-input-number
                v-model="form.baseSalary"
                :min="0"
                :max="100000"
                :step="100"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="绩效奖金" prop="performanceBonus">
              <el-input-number
                v-model="form.performanceBonus"
                :min="0"
                :max="50000"
                :step="100"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="津贴补助" prop="allowance">
              <el-input-number
                v-model="form.allowance"
                :min="0"
                :max="20000"
                :step="100"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ editingStandard ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh } from '@element-plus/icons-vue';
import { getPositionSalaryStandards, createPositionSalaryStandard, updatePositionSalaryStandard, deletePositionSalaryStandard } from '@/api/salary';

// 响应式数据
const loading = ref(false);
const submitting = ref(false);
const showAddDialog = ref(false);
const editingStandard = ref(null);
const formRef = ref();

// 分页数据
const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0);
const standardsList = ref([]);

// 表单数据
const form = reactive({
  position: '',
  department: '',
  level: '',
  baseSalary: 0,
  performanceBonus: 0,
  allowance: 0,
  status: 'active',
  remark: ''
});

// 计算总薪资
const totalSalary = computed(() => {
  return form.baseSalary + form.performanceBonus + form.allowance;
});

// 表单验证规则
const rules = reactive({
  position: [
    { required: true, message: '请输入职位名称', trigger: 'blur' }
  ],
  department: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  level: [
    { required: true, message: '请选择级别', trigger: 'change' }
  ],
  baseSalary: [
    { required: true, message: '请输入基本工资', trigger: 'blur' },
    { type: 'number', min: 1000, message: '基本工资不能少于1000元', trigger: 'blur' }
  ]
});

// 获取薪资标准列表
const fetchStandards = async () => {
  try {
    loading.value = true;
    const response = await getPositionSalaryStandards({
      page: currentPage.value,
      pageSize: pageSize.value
    });
    
    standardsList.value = response.data.map(item => ({
      ...item,
      totalSalary: item.baseSalary + item.performanceBonus + item.allowance
    }));
    total.value = response.total;
  } catch (error) {
    ElMessage.error('获取薪资标准失败');
  } finally {
    loading.value = false;
  }
};

// 刷新数据
const refreshData = () => {
  fetchStandards();
};

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
  fetchStandards();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  fetchStandards();
};

// 编辑薪资标准
const editStandard = (row) => {
  editingStandard.value = row;
  Object.assign(form, {
    position: row.position,
    department: row.department,
    level: row.level,
    baseSalary: row.baseSalary,
    performanceBonus: row.performanceBonus,
    allowance: row.allowance,
    status: row.status,
    remark: row.remark || ''
  });
  showAddDialog.value = true;
};

// 切换状态
const toggleStatus = async (row) => {
  try {
    const newStatus = row.status === 'active' ? 'inactive' : 'active';
    await updatePositionSalaryStandard(row.id, { status: newStatus });
    ElMessage.success('状态更新成功');
    fetchStandards();
  } catch (error) {
    ElMessage.error('状态更新失败');
  }
};

// 删除薪资标准
const deleteStandard = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除职位"${row.position}"的薪资标准吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    await deletePositionSalaryStandard(row.id);
    ElMessage.success('删除成功');
    fetchStandards();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    submitting.value = true;
    
    const formData = {
      ...form,
      totalSalary: totalSalary.value
    };
    
    if (editingStandard.value) {
      await updatePositionSalaryStandard(editingStandard.value.id, formData);
      ElMessage.success('更新成功');
    } else {
      await createPositionSalaryStandard(formData);
      ElMessage.success('添加成功');
    }
    
    showAddDialog.value = false;
    fetchStandards();
  } catch (error) {
    ElMessage.error(editingStandard.value ? '更新失败' : '添加失败');
  } finally {
    submitting.value = false;
  }
};

// 重置表单
const resetForm = () => {
  editingStandard.value = null;
  Object.assign(form, {
    position: '',
    department: '',
    level: '',
    baseSalary: 0,
    performanceBonus: 0,
    allowance: 0,
    status: 'active',
    remark: ''
  });
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 格式化日期
const formatDate = (date) => {
  return new Date(date).toLocaleString('zh-CN');
};

// 组件挂载时获取数据
onMounted(() => {
  fetchStandards();
});
</script>

<style lang="scss" scoped>
.salary-standards {
  .action-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
  }
  
  .salary-amount {
    font-weight: 600;
    color: #409eff;
    
    &.total {
      color: #67c23a;
      font-size: 1.1em;
    }
  }
  
  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
}
</style> 