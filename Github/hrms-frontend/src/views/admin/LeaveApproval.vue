<template>
  <div class="admin-leave-approval">
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
      <h1>请假审批</h1>
      <p>审批员工请假申请，查看审批历史记录</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card pending">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <h3>待审批</h3>
            <p class="stat-value">{{ statistics.pending }}</p>
            <span class="stat-desc">全部待处理</span>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card approved">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-info">
            <h3>已批准</h3>
            <p class="stat-value">{{ statistics.approved }}</p>
            <span class="stat-desc">本月批准</span>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card rejected">
        <div class="stat-content">
          <div class="stat-icon">
            <el-icon><Close /></el-icon>
          </div>
          <div class="stat-info">
            <h3>已拒绝</h3>
            <p class="stat-value">{{ statistics.rejected }}</p>
            <span class="stat-desc">本月拒绝</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-row">
        <el-input
          v-model="filters.employeeId"
          placeholder="搜索员工工号"
          clearable
          style="width: 180px"
          @input="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="filters.status" placeholder="申请状态" clearable @change="handleSearch">
          <el-option label="待审批" value="pending" />
          <el-option label="已批准" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
        
        <el-select v-model="filters.leaveType" placeholder="请假类型" clearable @change="handleSearch">
          <el-option 
            v-for="type in leaveTypes" 
            :key="type" 
            :label="type" 
            :value="type" 
          />
        </el-select>
        
        <el-button @click="resetFilters">
          <el-icon><RefreshRight /></el-icon>
          重置筛选
        </el-button>
        
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        
        <el-button @click="forceRefresh" type="primary">
          <el-icon><RefreshRight /></el-icon>
          强制刷新
        </el-button>
      </div>
    </el-card>

    <!-- 请假申请列表 -->
    <el-card class="table-card">
      <div class="table-header">
        <div class="header-left">
          <h3>请假申请列表</h3>
          <span class="search-info" v-if="hasActiveFilters">
            （已筛选，共 {{ pagination.total }} 条结果）
          </span>
        </div>
        <div class="header-actions">
          <!-- 批量操作功能已删除 -->
        </div>
      </div>
      
      <el-table 
        v-loading="loading" 
        :data="applicationList" 
        stripe
        @row-click="viewDetail"
      >

        
        <el-table-column prop="employeeId" label="员工工号" width="120" />
        
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
        
        <el-table-column prop="applyDate" label="创建时间" width="120" />
        
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">
              查看
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              type="success" 
              link 
              @click="approveApplication(row, 'approved')"
            >
              批准
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              type="danger" 
              link 
              @click="approveApplication(row, 'rejected')"
            >
              驳回
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

    <!-- 详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      title="请假申请详情" 
      width="700px"
    >
      <div v-if="currentDetail" class="detail-content">
        <!-- 申请人信息 -->
        <div class="detail-section">
          <h4>申请人信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <label>员工工号：</label>
              <span>{{ currentDetail.employeeId }}</span>
            </div>
            <div class="detail-item">
              <label>申请时间：</label>
              <span>{{ currentDetail.applyDate }}</span>
            </div>
          </div>
        </div>

        <!-- 请假信息 -->
        <div class="detail-section">
          <h4>请假信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <label>请假类型：</label>
              <el-tag :type="getLeaveTypeTagType(currentDetail.leaveType)">
                {{ currentDetail.leaveType }}
              </el-tag>
            </div>
            <div class="detail-item">
              <label>请假时间：</label>
              <span>{{ currentDetail.startDate }} 至 {{ currentDetail.endDate }}</span>
            </div>
            <div class="detail-item">
              <label>请假天数：</label>
              <span>{{ currentDetail.days }}天</span>
            </div>
            <div class="detail-item">
              <label>申请状态：</label>
              <el-tag :type="getStatusTagType(currentDetail.status)">
                {{ getStatusText(currentDetail.status) }}
              </el-tag>
            </div>
          </div>
          
          <div class="detail-row">
            <label>请假事由：</label>
            <p class="reason-text">{{ currentDetail.reason }}</p>
          </div>
        </div>

        <!-- 审批信息 -->
        <div v-if="currentDetail.approver || currentDetail.status !== 'pending'" class="detail-section">
          <h4>审批信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <label>审批人：</label>
              <span>{{ currentDetail.approver || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>审批时间：</label>
              <span>{{ currentDetail.approveDate || '-' }}</span>
            </div>
          </div>
          
          <div v-if="currentDetail.approveReason" class="detail-row">
            <label>审批意见：</label>
            <p class="reason-text">{{ currentDetail.approveReason }}</p>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button 
          v-if="currentDetail && currentDetail.status === 'pending'" 
          type="success" 
          @click="approveApplication(currentDetail, 'approved')"
        >
          批准
        </el-button>
        <el-button 
          v-if="currentDetail && currentDetail.status === 'pending'" 
          type="danger" 
          @click="approveApplication(currentDetail, 'rejected')"
        >
          驳回
        </el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog 
      v-model="showApprovalDialog" 
      :title="approvalAction === 'approved' ? '批准申请' : '驳回申请'" 
      width="600px"
      :close-on-click-modal="false"
    >
      <!-- 申请信息摘要 -->
      <div v-if="currentApprovalApplication" class="approval-summary">
        <h4>申请信息</h4>
        <div class="summary-grid">
          <div class="summary-item">
            <label>员工工号：</label>
            <span>{{ currentApprovalApplication.employeeId }}</span>
          </div>
          <div class="summary-item">
            <label>请假类型：</label>
            <el-tag :type="getLeaveTypeTagType(currentApprovalApplication.leaveType)">
              {{ currentApprovalApplication.leaveType }}
            </el-tag>
          </div>
          <div class="summary-item">
            <label>请假时间：</label>
            <span>{{ currentApprovalApplication.startDate }} 至 {{ currentApprovalApplication.endDate }}</span>
          </div>
          <div class="summary-item">
            <label>请假天数：</label>
            <span>{{ currentApprovalApplication.days }}天</span>
          </div>
        </div>
        <div class="summary-reason">
          <label>请假事由：</label>
          <p>{{ currentApprovalApplication.reason }}</p>
        </div>
      </div>
      
      <el-divider />
      
      <el-form 
        ref="approvalFormRef" 
        :model="approvalForm" 
        :rules="approvalRules" 
        label-width="100px"
      >
        <el-form-item label="审批意见" prop="reason">
          <el-input
            v-model="approvalForm.reason"
            type="textarea"
            :rows="4"
            :placeholder="approvalAction === 'approved' ? '请填写批准理由（可选）' : '请填写驳回理由'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showApprovalDialog = false">取消</el-button>
        <el-button 
          :type="approvalAction === 'approved' ? 'success' : 'danger'" 
          :loading="approving" 
          @click="submitApproval"
        >
          {{ approvalAction === 'approved' ? '确认批准' : '确认驳回' }}
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
  Clock, Check, Close, Document, User, Search, 
  RefreshRight, Refresh, ArrowLeft 
} from '@element-plus/icons-vue';
// API导入已移除，使用本地数据模拟

// 响应式数据
const router = useRouter();
const loading = ref(false);
const approving = ref(false);
const showDetailDialog = ref(false);
const showApprovalDialog = ref(false);
const approvalFormRef = ref();

// 返回上一页
const goBack = () => {
  router.back();
};

// 列表数据
const applicationList = ref([]);
const leaveTypes = ref(['年假', '病假', '事假', '婚假', '产假', '陪产假', '丧假']);
const currentDetail = ref(null);

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
});

// 筛选条件
const filters = reactive({
  employeeId: '',
  status: '',
  leaveType: ''
});

// 审批相关
const approvalAction = ref('approved'); // 'approved' 或 'rejected'
const currentApprovalApplication = ref(null);
const approvalForm = reactive({
  reason: ''
});

// 审批表单验证规则
const approvalRules = computed(() => ({
  reason: approvalAction.value === 'rejected' ? [
    { required: true, message: '驳回申请必须填写理由', trigger: 'blur' },
    { min: 5, message: '驳回理由至少5个字符', trigger: 'blur' }
  ] : []
}));

// 全部数据缓存（用于统计）
const allApplicationsCache = ref([]);

// 统计数据（基于全部数据，而非当前页面数据）
const statistics = computed(() => {
  const stats = {
    pending: 0,
    approved: 0,
    rejected: 0,
    unknown: 0
  };
  
  // 获取当前月份
  const currentMonth = new Date().getMonth() + 1;
  const currentYear = new Date().getFullYear();
  
  allApplicationsCache.value.forEach(item => {
    // 对于已批准和已拒绝，使用审批时间判断是否为本月
    const approveDate = item.approveDate ? new Date(item.approveDate) : null;
    
    const isApproveCurrentMonth = approveDate && 
                                 approveDate.getMonth() + 1 === currentMonth && 
                                 approveDate.getFullYear() === currentYear;
    
    // 调试信息：记录每个已拒绝的申请
    if (item.status === 'rejected') {
      console.log('🔍 已拒绝申请详情:', {
        id: item.id,
        employeeId: item.employeeId,
        approveDate: item.approveDate,
        approveYear: approveDate?.getFullYear(),
        approveMonth: approveDate?.getMonth() + 1,
        currentYear,
        currentMonth,
        isCurrentMonth: isApproveCurrentMonth
      });
    }
    
    // 统计所有待审批（不分月份）
    if (item.status === 'pending') {
      stats.pending++;
    }
    // 统计本月已批准（使用审批时间）
    else if (item.status === 'approved' && isApproveCurrentMonth) {
      stats.approved++;
    }
    // 统计本月已拒绝（使用审批时间）
    else if (item.status === 'rejected' && isApproveCurrentMonth) {
      stats.rejected++;
    }
    
    // 统计未知状态（全部数据中的未知状态）
    if (!['pending', 'approved', 'rejected'].includes(item.status)) {
      console.warn('⚠️ 统计时发现未知状态:', item.status, '申请ID:', item.id);
      stats.unknown++;
    }
  });
  
  // 开发环境调试信息
  if (process.env.NODE_ENV === 'development') {
    console.log('📊 请假申请统计结果 (' + currentYear + '-' + currentMonth + '):', {
      '全部数据总数': allApplicationsCache.value.length,
      '待审批(全部)': stats.pending,
      '本月已批准': stats.approved,
      '本月已拒绝': stats.rejected,
      '未知状态': stats.unknown
    });
  }
  
  return stats;
});

// 检测是否有活跃的筛选条件
const hasActiveFilters = computed(() => {
  return filters.employeeId || 
         filters.status || 
         filters.leaveType;
});

// 获取管理员信息
const getAdminInfo = () => {
  return {
    name: localStorage.getItem('user_name') || '系统管理员'
  };
};

// 数据映射函数
const mapApplicationData = (app) => {
  // 状态映射：数据库中文状态 -> 前端英文状态
  let mappedStatus = 'unknown'; // 默认为未知状态，避免错误映射
  
  // 待审批状态
  if (app.status === '待审批' || app.status === '待审核') {
    mappedStatus = 'pending';
  } 
  // 已批准状态
  else if (app.status === '批准' || app.status === '已批准') {
    mappedStatus = 'approved';
  } 
  // 已拒绝/驳回状态
  else if (app.status === '未批准' || app.status === '已拒绝' || app.status === '驳回' || app.status === '已驳回') {
    mappedStatus = 'rejected';
  }
  // 其他未知状态
  else {
    console.warn('⚠️ 发现未知请假申请状态:', app.status, '申请ID:', app.id);
    mappedStatus = 'unknown';
  }
  
  return {
    id: app.id,
    employeeId: app.empId,
    leaveType: app.type,
    startDate: app.startDate,
    endDate: app.endDate,
    days: app.days,
    reason: app.reason,
    status: mappedStatus, // 使用映射后的状态
    applyDate: app.createdAt || app.createTime, // 使用createdAt作为申请时间
    approver: app.approver,
    approveDate: app.approveTime,
    approveReason: app.remarks
  };
};

// 获取请假申请列表
const fetchApplications = async () => {
  try {
    loading.value = true;
    
    console.log('🔄 获取请假申请列表', { filters });
    
    // 调用真实的API接口
    const { getLeaveApplications } = await import('@/api/leave');
    
    // 添加时间戳防止缓存
    const timestamp = Date.now();
    const response = await getLeaveApplications({ _t: timestamp });
    
    console.log('✅ 请假申请列表获取成功:', response);
    
    let allApplications = [];
    
    // 处理响应数据
    if (response && Array.isArray(response)) {
      allApplications = response.map(app => mapApplicationData(app));
    } else if (response && response.data) {
      allApplications = response.data.map(app => mapApplicationData(app));
    } else {
      console.warn('⚠️ API返回数据格式异常:', response);
      allApplications = [];
    }
    
    // 更新全部数据缓存（用于统计）
    allApplicationsCache.value = allApplications;
    
    // 前端筛选逻辑
    let filteredApplications = allApplications;
    
    // 按员工工号筛选（模糊搜索）
    if (filters.employeeId) {
      filteredApplications = filteredApplications.filter(app => 
        app.employeeId && app.employeeId.includes(filters.employeeId)
      );
    }
    
    // 按状态筛选
    if (filters.status) {
      filteredApplications = filteredApplications.filter(app => 
        app.status === filters.status
      );
    }
    
    // 按请假类型筛选
    if (filters.leaveType) {
      filteredApplications = filteredApplications.filter(app => 
        app.leaveType === filters.leaveType
      );
    }
    
    // 分页处理
    const startIndex = (pagination.page - 1) * pagination.pageSize;
    const endIndex = startIndex + pagination.pageSize;
    
    applicationList.value = filteredApplications.slice(startIndex, endIndex);
    pagination.total = filteredApplications.length;
    
    console.log('�� 筛选后的数据:', {
      总数: allApplications.length,
      筛选后: filteredApplications.length,
      当前页: applicationList.value.length,
      筛选条件: filters,
      统计数据: statistics.value
    });
    
  } catch (error) {
    console.error('❌ 获取申请列表失败:', error);
    ElMessage.error('获取申请列表失败：' + (error.message || '未知错误'));
    
    // 出错时清空列表
    applicationList.value = [];
    allApplicationsCache.value = [];
    pagination.total = 0;
  } finally {
    loading.value = false;
  }
};

// 获取请假类型
const fetchLeaveTypes = async () => {
  try {
    // 使用本地数据，无需API调用
    ElMessage.success('请假类型加载成功');
  } catch (error) {
    ElMessage.error('获取请假类型失败：' + error.message);
  }
};

// 查看详情
const viewDetail = async (row) => {
  try {
    // 从原始数据中查找详情
    currentDetail.value = applicationList.value.find(item => item.id === row.id);
    showDetailDialog.value = true;
  } catch (error) {
    ElMessage.error('获取详情失败：' + error.message);
  }
};

// 审批申请
const approveApplication = (application, action) => {
  // 先关闭详情对话框（如果打开的话）
  showDetailDialog.value = false;
  
  currentApprovalApplication.value = application;
  approvalAction.value = action;
  approvalForm.reason = '';
  
  // 稍微延迟一下再打开审批对话框，确保详情对话框完全关闭
  setTimeout(() => {
    showApprovalDialog.value = true;
  }, 100);
};

// 提交审批
const submitApproval = async () => {
  try {
    if (approvalAction.value === 'rejected') {
      await approvalFormRef.value.validate();
    }
    
    approving.value = true;
    
    console.log('🔄 提交审批:', {
      id: currentApprovalApplication.value.id,
      action: approvalAction.value,
      reason: approvalForm.reason
    });
    
    // 动态导入API
    const { updateLeaveApplication } = await import('@/api/leave');
    
    // 获取当前管理员信息
    const adminInfo = getAdminInfo();
    
    // 构建审批数据 - 使用数据库中的中文状态
    const approvalData = {
      status: approvalAction.value === 'approved' ? '批准' : '未批准',
      approver: adminInfo.name || '管理员',
      remarks: approvalForm.reason || (approvalAction.value === 'approved' ? '申请批准' : '申请驳回')
    };
    
    // 调用API更新请假申请状态
    await updateLeaveApplication(currentApprovalApplication.value.id, approvalData);
    
    console.log('✅ 审批提交成功');
    
    ElMessage.success(
      approvalAction.value === 'approved' ? '申请已批准' : '申请已拒绝'
    );
    
    showApprovalDialog.value = false;
    showDetailDialog.value = false;
    
    // 刷新列表数据
    await fetchApplications();
    
    // 延迟一秒后再次刷新确保数据同步
    setTimeout(async () => {
      await fetchApplications();
      console.log('🔄 二次刷新完成，确保数据最新');
    }, 1000);
    
  } catch (error) {
    console.error('❌ 审批失败:', error);
    ElMessage.error('审批失败：' + (error.message || '未知错误'));
  } finally {
    approving.value = false;
  }
};





// 搜索
const handleSearch = () => {
  pagination.page = 1;
  fetchApplications();
};

// 重置筛选
const resetFilters = () => {
  Object.assign(filters, {
    employeeId: '',
    status: '',
    leaveType: ''
  });
  pagination.page = 1;
  fetchApplications();
};

// 刷新数据
const refreshData = () => {
  fetchApplications();
};

// 强制刷新（清除所有缓存）
const forceRefresh = async () => {
  try {
    loading.value = true;
    
    // 清除本地缓存
    allApplicationsCache.value = [];
    applicationList.value = [];
    
    ElMessage.info('正在强制刷新数据...');
    
    // 等待1秒再获取数据，确保后端数据同步
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    await fetchApplications();
    
    ElMessage.success('数据强制刷新成功！');
  } catch (error) {
    ElMessage.error('强制刷新失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.page = 1;
  fetchApplications();
};

const handleCurrentChange = (val) => {
  pagination.page = val;
  fetchApplications();
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
  fetchApplications();
});
</script>

<style lang="scss" scoped>
.admin-leave-approval {
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
  
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 24px;
    
    @media (max-width: 768px) {
      grid-template-columns: 1fr;
      gap: 16px;
    }
    
    @media (max-width: 1024px) and (min-width: 769px) {
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;
    }
    
          .stat-card {
        border-radius: 12px;
        border: none;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
        }
        
        :deep(.el-card__body) {
          padding: 24px;
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
      }
      
      &.pending .stat-icon {
        background: linear-gradient(135deg, #f39c12, #e67e22);
      }
      
      &.approved .stat-icon {
        background: linear-gradient(135deg, #27ae60, #2ecc71);
      }
      
      &.rejected .stat-icon {
        background: linear-gradient(135deg, #e74c3c, #c0392b);
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
          margin-bottom: 4px;
        }
        
        .stat-desc {
          font-size: 0.8rem;
          color: #95a5a6;
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
      flex-wrap: wrap;
      
      .el-select {
        width: 150px;
      }
      
      .el-date-editor {
        width: 250px;
      }
    }
  }
  
  .table-card {
    border-radius: 12px;
    border: none;
    
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;
        
        h3 {
          font-size: 1.2rem;
          font-weight: 600;
          color: #2c3e50;
          margin: 0;
        }
        
        .search-info {
          font-size: 0.9rem;
          color: #409eff;
          background: #e8f4fd;
          padding: 4px 12px;
          border-radius: 20px;
          font-weight: 500;
        }
      }
      
      .header-actions {
        display: flex;
        gap: 12px;
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
    .detail-section {
      margin-bottom: 24px;
      
      h4 {
        font-size: 1.1rem;
        font-weight: 600;
        color: #2c3e50;
        margin-bottom: 16px;
        padding-bottom: 8px;
        border-bottom: 2px solid #ecf0f1;
      }
      
      .detail-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;
        margin-bottom: 16px;
        
        .detail-item {
          display: flex;
          
          label {
            width: 100px;
            font-weight: 600;
            color: #7f8c8d;
            flex-shrink: 0;
          }
        }
      }
      
      .detail-row {
        display: flex;
        margin-bottom: 16px;
        
        label {
          width: 100px;
          font-weight: 600;
          color: #7f8c8d;
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
  
  /* 审批对话框样式 */
  .approval-summary {
    margin-bottom: 20px;
    
    h4 {
      margin: 0 0 16px 0;
      font-size: 16px;
      color: #333;
      font-weight: 600;
    }
    
    .summary-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
      margin-bottom: 16px;
    }
    
    .summary-item {
      display: flex;
      align-items: center;
      
      label {
        font-weight: 500;
        color: #666;
        margin-right: 8px;
        white-space: nowrap;
      }
      
      span {
        color: #333;
      }
    }
    
    .summary-reason {
      label {
        font-weight: 500;
        color: #666;
        display: block;
        margin-bottom: 8px;
      }
      
      p {
        margin: 0;
        padding: 12px;
        background-color: #f8f9fa;
        border-radius: 6px;
        color: #333;
        line-height: 1.5;
        min-height: 60px;
      }
    }
  }
}

@media (max-width: 768px) {
  .admin-leave-approval {
    padding: 16px;
    
    .stats-cards {
      grid-template-columns: repeat(2, 1fr);
    }
    
    .filter-row {
      flex-direction: column;
      align-items: stretch;
      
      .el-select,
      .el-date-editor {
        width: 100%;
        margin-bottom: 12px;
      }
    }
    
    .table-header {
      flex-direction: column;
      align-items: stretch;
      
      .header-actions {
        margin-top: 12px;
        justify-content: center;
      }
    }
    
    .detail-grid {
      grid-template-columns: 1fr !important;
    }
    
    .summary-grid {
      grid-template-columns: 1fr !important;
    }
  }
}
</style> 