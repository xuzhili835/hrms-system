<template>
  <div class="employee-profile">
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

    <!-- 页面标题区域 -->
    <div class="page-header">
      <h1 class="page-title">个人资料</h1>
      <p class="page-desc">查看和管理您的个人信息</p>
    </div>

    <!-- 个人资料卡片 -->
    <div class="profile-content">
      <el-card class="profile-main-card">
        <!-- 头像和基本信息区域 -->
        <div class="profile-header">
          <div class="avatar-section">
            <el-avatar :size="100" :src="userProfile.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="basic-info">
              <h2>{{ userProfile.name || '未设置' }}</h2>
              <p class="emp-id">工号：{{ userProfile.empId || '未设置' }}</p>
              <el-tag :type="getStatusType(workInfo.status)">
                {{ workInfo.status || '未知状态' }}
              </el-tag>
            </div>
          </div>
          <div class="action-buttons">
            <el-button 
              @click="showChangePasswordDialog"
              :icon="Lock"
            >
              修改密码
            </el-button>
          </div>
        </div>

        <!-- 详细信息区域 -->
        <el-divider />
        
        <div class="profile-details">
          <el-row :gutter="32">
            <!-- 基本信息 -->
            <el-col :span="12">
              <div class="info-section">
                <h3 class="section-title">
                  <el-icon><User /></el-icon>
                  基本信息
                </h3>
                <el-form 
                  :model="userProfile" 
                  label-width="80px"
                >
                  <el-form-item label="姓名" prop="name">
                    <el-input 
                      v-model="userProfile.name" 
                      disabled
                      placeholder="姓名"
                    />
                  </el-form-item>
                  <el-form-item label="员工工号">
                    <el-input 
                      v-model="userProfile.empId" 
                      disabled
                      placeholder="员工工号"
                    />
                  </el-form-item>
                </el-form>
              </div>
            </el-col>

            <!-- 工作信息 -->
            <el-col :span="12">
              <div class="info-section">
                <h3 class="section-title">
                  <el-icon><Briefcase /></el-icon>
                  工作信息
                </h3>
                <div class="info-grid">
                  <div class="info-item">
                    <label>所属部门</label>
                    <span>{{ workInfo.department || '未设置' }}</span>
                  </div>
                  <div class="info-item">
                    <label>职位</label>
                    <span>{{ workInfo.position || '未设置' }}</span>
                  </div>
                  <div class="info-item">
                    <label>入职日期</label>
                    <span>{{ workInfo.hireDate || '未设置' }}</span>
                  </div>
                  <div class="info-item">
                    <label>工作年限</label>
                    <span>{{ workInfo.workYears || '未计算' }}</span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>

          <!-- 安全提示 -->
          <el-divider />
          
          <div class="security-tips">
            <h3 class="section-title">
              <el-icon><Lock /></el-icon>
              安全建议
            </h3>
            <div class="tips-content">
              <el-row :gutter="16">
                <el-col :span="12">
                  <div class="tip-item">
                    <el-icon class="tip-icon"><Key /></el-icon>
                    <div>
                      <h4>定期更改密码</h4>
                      <p>建议每3-6个月更换一次登录密码</p>
                    </div>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="tip-item">
                    <el-icon class="tip-icon"><Lock /></el-icon>
                    <div>
                      <h4>密码安全要求</h4>
                      <p>密码长度不少于6位</p>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog 
      v-model="showPasswordDialog" 
      title="修改密码" 
      width="400px"
      @close="resetPasswordForm"
    >
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
            @keyup.enter="handleChangePassword"
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
            @keyup.enter="handleChangePassword"
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            @keyup.enter="handleChangePassword"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showPasswordDialog = false" :disabled="passwordLoading">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { 
  User, ArrowLeft, Lock, Briefcase, InfoFilled, Key
} from '@element-plus/icons-vue';
import { changePassword } from '@/api/auth';
import { getEmployeeById } from '@/api/employee';

// 响应式数据定义
const router = useRouter();
const passwordFormRef = ref(); // 密码表单引用
const showPasswordDialog = ref(false); // 是否显示修改密码对话框
const passwordLoading = ref(false); // 密码修改加载状态

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

// 用户个人资料数据
const userProfile = reactive({
  name: '',
  empId: '',
  avatar: ''
});

// 工作信息数据（只读）
const workInfo = reactive({
  employeeId: '',
  department: '',
  position: '',
  hireDate: '',
  workYears: '',
  status: ''
});

// 密码修改表单数据
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 基本信息表单验证规则
const basicFormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ]
};

// 密码修改表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && passwordForm.oldPassword && value === passwordForm.oldPassword) {
          callback(new Error('新密码不能与原密码相同'));
        } else {
          if (passwordForm.confirmPassword !== '') {
            passwordFormRef.value?.validateField('confirmPassword');
          }
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case '在职':
      return 'success';
    case '离职':
      return 'danger';
    case '试用期':
      return 'warning';
    default:
      return 'info';
  }
};

// 页面挂载时获取用户信息
onMounted(async () => {
  try {
    console.log('🎯 Profile页面开始初始化');
    
    // 从本地存储获取用户信息
    const storedUserName = localStorage.getItem('user_name');
    const storedUserInfo = JSON.parse(localStorage.getItem('user_info') || '{}');
    const empId = localStorage.getItem('user_employee_id');
    
    console.log('📋 本地存储信息:', {
      storedUserName,
      storedUserInfo,
      empId
    });
    
    // 设置基本信息
    if (storedUserName) {
      userProfile.name = storedUserName;
    }
    if (empId) {
      userProfile.empId = empId;
    }
    
    // 尝试从API获取最新信息
    if (empId) {
      try {
        const employeeResponse = await getEmployeeById(empId);
        const employeeData = employeeResponse?.data || employeeResponse;
        
        console.log('📡 从API获取的员工信息:', employeeData);
        
        // 更新用户信息
        if (employeeData.name) {
          userProfile.name = employeeData.name;
        }
        userProfile.empId = employeeData.empId || empId;
        
        // 更新工作信息
        workInfo.employeeId = employeeData.empId || empId;
        workInfo.department = employeeData.dept || '';
        workInfo.position = employeeData.pos || '';
        workInfo.status = employeeData.status || '';
        
        if (employeeData.entryTime || employeeData.entry_time) {
          const entryTime = employeeData.entryTime || employeeData.entry_time;
          workInfo.hireDate = entryTime;
          
          // 计算工作年限
          const entryDate = new Date(entryTime);
          const currentDate = new Date();
          const workYears = (currentDate - entryDate) / (365.25 * 24 * 60 * 60 * 1000);
          workInfo.workYears = Math.round(workYears * 10) / 10 + '年';
        }
        
        console.log('✅ 用户信息更新完成:', { userProfile, workInfo });
        
      } catch (error) {
        console.warn('⚠️ 从API获取员工信息失败，使用本地缓存:', error);
      }
    }
    
  } catch (error) {
    console.error('❌ Profile页面初始化失败:', error);
  }
});

/**
 * 显示修改密码对话框
 */
const showChangePasswordDialog = () => {
  showPasswordDialog.value = true;
};

/**
 * 重置密码表单
 */
const resetPasswordForm = () => {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordFormRef.value?.resetFields();
};

/**
 * 处理密码修改
 */
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate();
    
    passwordLoading.value = true;
    
    // 调用修改密码API
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });
    
    ElMessage.success('密码修改成功！');
    showPasswordDialog.value = false;
    resetPasswordForm();
    
  } catch (error) {
    console.error('密码修改失败:', error);
    ElMessage.error(error.message || '密码修改失败，请重试');
  } finally {
    passwordLoading.value = false;
  }
};
</script>

<style scoped>
.employee-profile {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 返回按钮 */
.back-button-container {
  margin-bottom: 20px;
}

.back-button {
  background-color: #ffffff;
  border: 1px solid #dcdfe6;
  color: #606266;
  transition: all 0.3s;
}

.back-button:hover {
  background-color: #409eff;
  border-color: #409eff;
  color: #ffffff;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
  text-align: center;
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.page-desc {
  color: #7f8c8d;
  font-size: 1rem;
  margin: 0;
}

/* 主要内容区域 */
.profile-content {
  max-width: 1000px;
  margin: 0 auto;
}

.profile-main-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 个人资料头部 */
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-section .el-avatar {
  border: 3px solid #f0f0f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.basic-info h2 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin: 0 0 8px 0;
}

.basic-info .emp-id {
  color: #7f8c8d;
  margin: 0 0 12px 0;
  font-size: 0.95rem;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

/* 详细信息区域 */
.profile-details {
  padding-top: 24px;
}

.info-section {
  margin-bottom: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  color: #2c3e50;
  margin-bottom: 20px;
  font-weight: 600;
}

.section-title .el-icon {
  color: #409eff;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item label {
  font-size: 0.9rem;
  color: #7f8c8d;
  font-weight: 500;
}

.info-item span {
  font-size: 1rem;
  color: #2c3e50;
  font-weight: 500;
}

/* 功能说明区域 */
.feature-notes {
  margin-bottom: 32px;
}

/* 安全提示区域 */
.security-tips {
  margin-bottom: 0;
}

.tips-content {
  margin-top: 16px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.tip-icon {
  color: #409eff;
  font-size: 1.2rem;
  margin-top: 2px;
}

.tip-item h4 {
  color: #2c3e50;
  font-size: 1rem;
  margin: 0 0 4px 0;
}

.tip-item p {
  color: #7f8c8d;
  font-size: 0.9rem;
  margin: 0;
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .employee-profile {
    padding: 16px;
  }
  
  .profile-header {
    flex-direction: column;
    gap: 20px;
    align-items: flex-start;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .action-buttons {
    width: 100%;
    justify-content: center;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .el-row .el-col {
    margin-bottom: 16px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 1.5rem;
  }
  
  .avatar-section .el-avatar {
    width: 80px !important;
    height: 80px !important;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style> 