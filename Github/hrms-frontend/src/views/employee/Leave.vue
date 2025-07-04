<template>
  <div class="employee-leave">
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
      <h1>请假申请</h1>
      <p>提交请假申请，查看申请状态和历史记录</p>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <el-button type="primary" @click="showApplyDialog = true">
        <el-icon><Plus /></el-icon>
        申请请假
      </el-button>
      <el-button @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <h3>待审批</h3>
            <p class="stat-value">{{ statistics.pending }}</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon approved">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-info">
            <h3>已批准</h3>
            <p class="stat-value">{{ statistics.approved }}</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon rejected">
            <el-icon><Close /></el-icon>
          </div>
          <div class="stat-info">
            <h3>已拒绝</h3>
            <p class="stat-value">{{ statistics.rejected }}</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon total">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <h3>总申请</h3>
            <p class="stat-value">{{ statistics.total }}</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-row">
        <el-select v-model="filters.status" placeholder="申请状态" clearable>
          <el-option label="待审批" value="pending" />
          <el-option label="已批准" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
        
        <el-select v-model="filters.leaveType" placeholder="请假类型" clearable>
          <el-option 
            v-for="type in leaveTypes" 
            :key="type.value" 
            :label="type.label" 
            :value="type.value" 
          />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        
        <el-button @click="resetFilters">
          <el-icon><RefreshRight /></el-icon>
          重置
        </el-button>
      </div>
    </el-card>

    <!-- 请假记录列表 -->
    <el-card class="table-card">
      <div class="table-header">
        <h3>我的请假记录</h3>
      </div>
      
      <el-table 
        v-loading="loading" 
        :data="leaveList" 
        stripe
        @row-click="viewDetail"
      >
        <el-table-column prop="leaveType" label="请假类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getLeaveTypeTagType(row.leaveType)">
              {{ row.leaveType }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="days" label="天数" width="80" align="center" />
        
        <el-table-column prop="reason" label="请假事由" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="applyDate" label="申请时间" width="120" />
        
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">
              查看
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              type="danger" 
              link 
              @click="cancelApplication(row)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 申请请假对话框 -->
    <el-dialog 
      v-model="showApplyDialog" 
      title="申请请假" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="applyFormRef" 
        :model="applyForm" 
        :rules="applyRules" 
        label-width="100px"
      >
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="applyForm.leaveType" placeholder="请选择请假类型">
            <el-option 
              v-for="type in leaveTypes" 
              :key="type.value" 
              :label="`${type.label} (最多${type.maxDays}天)`" 
              :value="type.value" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="applyForm.startDate"
            type="date"
            placeholder="选择开始日期"
            :disabled-date="disabledDate"
            @change="calculateDays"
          />
        </el-form-item>
        
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="applyForm.endDate"
            type="date"
            placeholder="选择结束日期"
            :disabled-date="disabledDate"
            @change="calculateDays"
          />
        </el-form-item>
        
        <el-form-item label="请假天数">
          <el-input v-model="applyForm.days" readonly>
            <template #suffix>天</template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="请假事由" prop="reason">
          <el-input
            v-model="applyForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明请假原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitApplication">
          提交申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      title="请假申请详情" 
      width="600px"
    >
      <div v-if="currentDetail" class="detail-content">
        <div class="detail-row">
          <label>请假类型：</label>
          <el-tag :type="getLeaveTypeTagType(currentDetail.leaveType)">
            {{ currentDetail.leaveType }}
          </el-tag>
        </div>
        
        <div class="detail-row">
          <label>请假时间：</label>
          <span>{{ currentDetail.startDate }} 至 {{ currentDetail.endDate }} ({{ currentDetail.days }}天)</span>
        </div>
        
        <div class="detail-row">
          <label>申请状态：</label>
          <el-tag :type="getStatusTagType(currentDetail.status)">
            {{ getStatusText(currentDetail.status) }}
          </el-tag>
        </div>
        
        <div class="detail-row">
          <label>申请时间：</label>
          <span>{{ currentDetail.applyDate }}</span>
        </div>
        
        <div class="detail-row">
          <label>请假事由：</label>
          <p class="reason-text">{{ currentDetail.reason }}</p>
        </div>
        
        <div v-if="currentDetail.approver" class="detail-row">
          <label>审批人：</label>
          <span>{{ currentDetail.approver }}</span>
        </div>
        
        <div v-if="currentDetail.approveDate" class="detail-row">
          <label>审批时间：</label>
          <span>{{ currentDetail.approveDate }}</span>
        </div>
        
        <div v-if="currentDetail.approveReason" class="detail-row">
          <label>审批意见：</label>
          <p class="reason-text">{{ currentDetail.approveReason }}</p>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button 
          v-if="currentDetail && currentDetail.status === 'pending'" 
          type="danger" 
          @click="cancelApplication(currentDetail)"
        >
          撤销申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Plus, Refresh, Clock, Check, Close, Document, 
  Search, RefreshRight, ArrowLeft 
} from '@element-plus/icons-vue';
import { 
  getEmployeeLeaveHistory, 
  submitLeaveApplication, 
  cancelLeaveApplication,
  getLeaveTypes,
  getLeaveApplicationDetail
} from '@/api/leave';

// 响应式数据
const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const showApplyDialog = ref(false);
const showDetailDialog = ref(false);
const applyFormRef = ref();

// 返回上一页
const goBack = () => {
  // 如果有历史记录且不是登录页面，则返回上一页
  if (window.history.length > 1 && !document.referrer.includes('/login')) {
    router.back();
  } else {
    // 否则返回员工仪表板
    router.push('/employee/dashboard');
  }
};

// 列表数据
const leaveList = ref([]);
const leaveTypes = ref([]);
const currentDetail = ref(null);

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
});

// 筛选条件
const filters = reactive({
  status: '',
  leaveType: ''
});

// 申请表单
const applyForm = reactive({
  leaveType: '',
  startDate: '',
  endDate: '',
  days: 0,
  reason: ''
});

// 表单验证规则
const applyRules = {
  leaveType: [
    { required: true, message: '请选择请假类型', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请填写请假事由', trigger: 'blur' },
    { min: 5, message: '请假事由至少5个字符', trigger: 'blur' }
  ]
};

// 统计数据
const statistics = computed(() => {
  const stats = {
    pending: 0,
    approved: 0,
    rejected: 0,
    total: 0
  };
  
  leaveList.value.forEach(item => {
    stats[item.status]++;
    stats.total++;
  });
  
  return stats;
});

// 获取用户信息
const getUserInfo = () => {
  return {
    id: parseInt(localStorage.getItem('user_id') || '1'),
    employeeId: localStorage.getItem('user_employee_id') || 'EMP001',
    name: localStorage.getItem('user_name') || '张三',
    department: localStorage.getItem('user_department') || '技术部'
  };
};

// 获取请假记录
const fetchLeaveHistory = async () => {
  try {
    loading.value = true;
    const userInfo = getUserInfo();
    const params = {
      ...filters,
      page: pagination.page,
      pageSize: pagination.pageSize
    };
    
    const response = await getEmployeeLeaveHistory(userInfo.employeeId, params);
    
    // 确保只显示当前员工的请假记录，并处理字段映射
    const filteredData = (response.data || []).filter(item => 
      item.empId === userInfo.employeeId
    ).map(item => {
      // 处理状态映射，支持多种可能的状态值
      let mappedStatus = 'unknown'; // 默认为未知状态
      
      // 待审批状态
      if (item.status === '待审核' || item.status === '待审批' || item.status === 'pending') {
        mappedStatus = 'pending';
      } 
      // 已批准状态
      else if (item.status === '已批准' || item.status === '已通过' || item.status === '批准' || item.status === 'approved') {
        mappedStatus = 'approved';
      } 
      // 已拒绝/驳回状态
      else if (item.status === '已拒绝' || item.status === '未批准' || item.status === '驳回' || item.status === 'rejected') {
        mappedStatus = 'rejected';
      }
      // 其他未知状态
      else {
        console.warn('⚠️ 员工端发现未知请假申请状态:', item.status, '申请ID:', item.id);
        mappedStatus = 'unknown';
      }
      
      return {
        ...item,
        leaveType: item.type || item.leaveType, // 字段映射：type -> leaveType
        status: mappedStatus
      };
    });
    
    // 应用前端筛选
    let finalData = filteredData;
    
    // 按状态筛选
    if (filters.status) {
      finalData = finalData.filter(item => item.status === filters.status);
    }
    
    // 按请假类型筛选
    if (filters.leaveType) {
      finalData = finalData.filter(item => item.leaveType === filters.leaveType);
    }
    
    console.log('📅 请假记录数据处理:', {
      employeeId: userInfo.employeeId,
      totalRecords: response.data?.length || 0,
      filteredRecords: filteredData.length,
      finalFilteredRecords: finalData.length,
      appliedFilters: filters,
      data: finalData
    });
    
    leaveList.value = finalData;
    pagination.total = finalData.length;
  } catch (error) {
    ElMessage.error('获取请假记录失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 获取请假类型
const fetchLeaveTypes = async () => {
  try {
    const response = await getLeaveTypes();
    leaveTypes.value = response;
  } catch (error) {
    ElMessage.error('获取请假类型失败：' + error.message);
  }
};

// 计算请假天数
const calculateDays = () => {
  if (applyForm.startDate && applyForm.endDate) {
    const start = new Date(applyForm.startDate);
    const end = new Date(applyForm.endDate);
    const diffTime = Math.abs(end - start);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    applyForm.days = diffDays;
  } else {
    applyForm.days = 0;
  }
};

// 禁用日期（不能选择过去的日期）
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7; // 昨天之前的日期
};

// 提交申请
const submitApplication = async () => {
  try {
    await applyFormRef.value.validate();
    
    // 验证请假天数
    const selectedType = leaveTypes.value.find(type => type.value === applyForm.leaveType);
    if (selectedType && applyForm.days > selectedType.maxDays) {
      ElMessage.error(`${applyForm.leaveType}最多只能请${selectedType.maxDays}天`);
      return;
    }
    
    submitting.value = true;
    const userInfo = getUserInfo();
    
    // 格式化日期为 YYYY-MM-DD 格式
    const formatDate = (date) => {
      if (!date) return '';
      const d = new Date(date);
      return d.getFullYear() + '-' + 
             String(d.getMonth() + 1).padStart(2, '0') + '-' + 
             String(d.getDate()).padStart(2, '0');
    };

    const applicationData = {
      employeeId: userInfo.employeeId,
      employeeName: userInfo.name,
      department: userInfo.department,
      leaveType: applyForm.leaveType,
      startDate: formatDate(applyForm.startDate),
      endDate: formatDate(applyForm.endDate),
      days: applyForm.days,
      reason: applyForm.reason
    };
    
    await submitLeaveApplication(applicationData);
    ElMessage.success('请假申请提交成功');
    
    showApplyDialog.value = false;
    resetApplyForm();
    fetchLeaveHistory();
  } catch (error) {
    ElMessage.error('提交申请失败：' + error.message);
  } finally {
    submitting.value = false;
  }
};

// 重置申请表单
const resetApplyForm = () => {
  Object.assign(applyForm, {
    leaveType: '',
    startDate: '',
    endDate: '',
    days: 0,
    reason: ''
  });
  applyFormRef.value?.resetFields();
};

// 查看详情
const viewDetail = async (row) => {
  try {
    const response = await getLeaveApplicationDetail(row.id);
    currentDetail.value = response;
    showDetailDialog.value = true;
  } catch (error) {
    ElMessage.error('获取详情失败：' + error.message);
  }
};

// 撤销申请
const cancelApplication = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要撤销这个请假申请吗？撤销后无法恢复。',
      '确认撤销',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    const userInfo = getUserInfo();
    await cancelLeaveApplication(row.id, userInfo.employeeId);
    ElMessage.success('申请已撤销');
    
    showDetailDialog.value = false;
    fetchLeaveHistory();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤销申请失败：' + error.message);
    }
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  fetchLeaveHistory();
};

// 重置筛选
const resetFilters = () => {
  Object.assign(filters, {
    status: '',
    leaveType: ''
  });
  pagination.page = 1;
  fetchLeaveHistory();
};

// 刷新数据
const refreshData = () => {
  fetchLeaveHistory();
};

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.page = 1;
  fetchLeaveHistory();
};

const handleCurrentChange = (val) => {
  pagination.page = val;
  fetchLeaveHistory();
};

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    unknown: 'info'
  };
  return typeMap[status] || 'info';
};

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    pending: '待审批',
    approved: '已批准',
    rejected: '已拒绝',
    unknown: '未知状态'
  };
  return textMap[status] || status;
};

// 获取请假类型标签类型
const getLeaveTypeTagType = (leaveType) => {
  const typeMap = {
    '年假': 'success',
    '病假': 'warning',
    '事假': 'info',
    '婚假': 'danger',
    '产假': 'success',
    '陪产假': 'primary',
    '丧假': 'info'
  };
  return typeMap[leaveType] || '';
};

// 页面挂载
onMounted(() => {
  fetchLeaveTypes();
  fetchLeaveHistory();
});
</script>

<style lang="scss" scoped>
.employee-leave {
  padding: 24px;
  
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
  
  // 返回按钮样式已移至全局样式，此处删除局部样式以避免冲突
  
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
  
  .quick-actions {
    margin-bottom: 24px;
    
    .el-button {
      margin-right: 12px;
    }
  }
  
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 24px;
    
    .stat-card {
      border-radius: 12px;
      border: none;
      
      :deep(.el-card__body) {
        padding: 20px;
      }
      
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;
      }
      
      .stat-icon {
        width: 50px;
        height: 50px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        
        .el-icon {
          font-size: 20px;
          color: white;
        }
        
        &.pending {
          background: linear-gradient(135deg, #f39c12, #e67e22);
        }
        
        &.approved {
          background: linear-gradient(135deg, #27ae60, #2ecc71);
        }
        
        &.rejected {
          background: linear-gradient(135deg, #e74c3c, #c0392b);
        }
        
        &.total {
          background: linear-gradient(135deg, #3498db, #2980b9);
        }
      }
      
      .stat-info {
        flex: 1;
        
        h3 {
          font-size: 0.9rem;
          color: #7f8c8d;
          margin-bottom: 4px;
          font-weight: 500;
        }
        
        .stat-value {
          font-size: 1.8rem;
          font-weight: 700;
          color: #2c3e50;
          margin: 0;
        }
      }
    }
  }
  
  .filter-card {
    margin-bottom: 24px;
    border-radius: 12px;
    border: none;
    
    .filter-row {
      display: flex;
      gap: 16px;
      align-items: center;
      
      .el-select {
        width: 150px;
      }
    }
  }
  
  .table-card {
    border-radius: 12px;
    border: none;
    
    .table-header {
      margin-bottom: 16px;
      
      h3 {
        font-size: 1.2rem;
        font-weight: 600;
        color: #2c3e50;
        margin: 0;
      }
    }
    
    :deep(.el-table) {
      .el-table__row {
        cursor: pointer;
        
        &:hover {
          background-color: #f8f9fa;
        }
      }
    }
    
    .pagination-wrapper {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
  }
  
  .detail-content {
    .detail-row {
      display: flex;
      margin-bottom: 16px;
      
      label {
        width: 100px;
        font-weight: 600;
        color: #2c3e50;
        flex-shrink: 0;
      }
      
      .reason-text {
        margin: 0;
        line-height: 1.6;
        color: #34495e;
        white-space: pre-wrap;
      }
    }
  }
}

@media (max-width: 768px) {
  .employee-leave {
    padding: 16px;
    
    .stats-cards {
      grid-template-columns: repeat(2, 1fr);
    }
    
    .filter-row {
      flex-direction: column;
      align-items: stretch;
      
      .el-select {
        width: 100%;
        margin-bottom: 12px;
      }
    }
  }
}
</style> 