<template>
  <div class="admin-settings">
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
      <h1 class="page-title">个人设置</h1>
      <p class="page-desc">管理您的个人信息和账户设置</p>
    </div>

    <!-- 设置内容区域 -->
    <div class="settings-content">
      <!-- 个人信息卡片 -->
      <el-card class="settings-card">
        <template #header>
          <div class="card-header">
            <h3>个人信息</h3>
          </div>
        </template>

        <div class="profile-form">
          <!-- 管理员基本信息显示 -->
          <div class="admin-info">
            <div class="info-item">
              <span class="label">当前用户：</span>
              <span class="value">{{ adminProfile.username || 'admin' }}</span>
            </div>
            <div class="info-item">
              <span class="label">角色身份：</span>
              <span class="value role">{{ getRoleText(adminProfile.role) }}</span>
            </div>
            <div class="info-item" v-if="adminProfile.createdAt">
              <span class="label">注册日期：</span>
              <span class="value">{{ formatDate(adminProfile.createdAt) }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 密码修改卡片 -->
      <el-card class="settings-card">
        <template #header>
          <div class="card-header">
            <h3>密码管理</h3>
            <el-button 
              type="primary" 
              size="small" 
              @click="showChangePasswordDialog"
              :icon="Lock"
            >
              修改密码
            </el-button>
          </div>
        </template>

        <div class="password-info">
          <p>为了账户安全，建议您定期更改密码</p>
          <div class="password-tips">
            <h4>密码设置建议：</h4>
            <ul>
              <li>长度不少于6位字符</li>
              <li>包含字母和数字</li>
              <li>避免使用常见密码</li>
              <li>定期更换密码</li>
            </ul>
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
import { User, ArrowLeft, Lock } from '@element-plus/icons-vue';
import { changeAdminPassword, getCurrentAdmin } from '@/api/admin';

// 响应式数据定义
const router = useRouter();
const passwordFormRef = ref(); // 密码表单引用
const showPasswordDialog = ref(false); // 是否显示修改密码对话框
const passwordLoading = ref(false); // 密码修改加载状态

// 管理员信息
const adminProfile = reactive({
  username: '',
  role: 'ADMIN',
  createdAt: null
});

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 方法定义
const goBack = () => {
  router.go(-1);
};

const showChangePasswordDialog = () => {
  showPasswordDialog.value = true;
};

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate();
    
    passwordLoading.value = true;
    
    await changeAdminPassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });
    
    ElMessage.success('密码修改成功');
    showPasswordDialog.value = false;
    resetPasswordForm();
  } catch (error) {
    ElMessage.error('密码修改失败：' + (error.message || '未知错误'));
  } finally {
    passwordLoading.value = false;
  }
};

const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate();
  }
};

// 获取角色显示文本
const getRoleText = (role) => {
  switch (role) {
    case 'ADMIN':
      return '系统管理员';
    case 'SUPER_ADMIN':
      return '超级管理员';
    default:
      return '管理员';
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  } catch (error) {
    console.warn('日期格式化失败:', error);
    return dateString;
  }
};

// 加载管理员信息
const loadAdminProfile = async () => {
  try {
    console.log('🔍 AdminSettings 开始加载管理员信息');
    
    // 先从API获取完整的管理员信息
    const adminData = await getCurrentAdmin();
    
    // 更新响应式数据
    Object.assign(adminProfile, {
      username: adminData.username,
      role: adminData.role,
      createdAt: adminData.createdAt
    });
    
    console.log('✅ AdminSettings 管理员信息已加载:', adminProfile);
    
  } catch (error) {
    console.error('❌ AdminSettings 加载管理员信息失败:', error);
    
    // API调用失败时，使用本地存储的基本信息
    const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}');
    const storedUsername = localStorage.getItem('user_name');
    
    adminProfile.username = storedUsername || userInfo.username || 'admin';
    adminProfile.role = userInfo.role || 'ADMIN';
    adminProfile.createdAt = null;
    
    console.log('⚠️ AdminSettings 使用本地缓存信息:', adminProfile);
  }
};

// 组件挂载
onMounted(async () => {
  await loadAdminProfile();
});
</script>

<style lang="scss" scoped>
/* 页面容器样式 */
.admin-settings {
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

/* 页面标题区域 */
.page-header {
  margin-bottom: 24px;
  padding-left: 60px; // 为返回按钮留出空间
  
  .page-title {
    font-size: 1.8rem;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 8px;
  }
  
  .page-desc {
    color: #7f8c8d;
    font-size: 1rem;
    margin: 0;
  }
}

/* 设置内容区域 */
.settings-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 设置卡片样式 */
.settings-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  
  /* 卡片头部样式 */
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h3 {
      font-size: 1.2rem;
      font-weight: 600;
      color: #2c3e50;
      margin: 0;
    }
  }
}

/* 个人资料表单样式 */
.profile-form {
  .admin-info {
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    
    .info-item {
      display: flex;
      align-items: center;
      padding: 16px 20px;
      background-color: #f8f9fa;
      border-radius: 8px;
      border-left: 4px solid #409eff;
      
      .label {
        font-weight: 600;
        color: #2c3e50;
        margin-right: 12px;
        min-width: 80px;
      }
      
      .value {
        color: #409eff;
        font-weight: 500;
        font-size: 1.1rem;
        
        &.role {
          background: linear-gradient(45deg, #409eff, #67c23a);
          background-clip: text;
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          font-weight: 600;
        }
      }
    }
  }
}

/* 密码管理样式 */
.password-info {
  p {
    color: #7f8c8d;
    font-size: 1rem;
    margin-bottom: 20px;
  }
  
  .password-tips {
    background-color: #f8f9fa;
    padding: 16px;
    border-radius: 8px;
    border-left: 4px solid #3498db;
    
    h4 {
      color: #2c3e50;
      font-size: 1rem;
      margin-bottom: 12px;
    }
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        color: #5a6c7d;
        font-size: 0.9rem;
        line-height: 1.6;
        margin-bottom: 4px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-settings {
    padding: 16px;
  }
  
  .page-header {
    .page-title {
      font-size: 1.5rem;
    }
  }
  
  .profile-form-content {
    .el-row {
      .el-col {
        flex: 0 0 100% !important;
        max-width: 100% !important;
      }
    }
  }
}

@media (max-width: 480px) {
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start !important;
  }
}
</style> 