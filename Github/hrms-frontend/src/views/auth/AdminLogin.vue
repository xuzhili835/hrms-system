<template>
  <div class="admin-login-container">
    <div class="login-background"></div>
    <div class="login-content">
      <div class="login-card">
        <div class="login-header">
          <div class="admin-badge">
            <el-icon><UserFilled /></el-icon>
            <span>管理员登录</span>
          </div>
          <h1>人力资源管理系统</h1>
          <p>管理员控制台</p>
        </div>
        
        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="管理员账号"
              size="large"
              prefix-icon="User"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="管理员密码"
              size="large"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          

          
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              class="login-btn"
              @click="handleLogin"
              :loading="loading"
            >
              登录管理后台
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <div class="other-options">
            <el-link type="primary" @click="goToEmployeeLogin">员工登录</el-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { UserFilled } from '@element-plus/icons-vue';
import { useAuthStore } from '@/store/auth';
import { useRouter } from 'vue-router';

const router = useRouter();
const authStore = useAuthStore();
const loginFormRef = ref();
const loading = ref(false);

const loginForm = reactive({
  username: '',
  password: '',
});

const rules = reactive({
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
});

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  loading.value = true;
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      const success = await authStore.adminLogin(loginForm);
      if (success) {
        ElMessage.success('管理员登录成功！');
      } else {
        ElMessage.error(authStore.error || '登录失败，请检查用户名和密码');
      }
    } else {
      ElMessage.error('请检查输入项是否正确');
    }
    loading.value = false;
  });
};

// 跳转到员工登录
const goToEmployeeLogin = () => {
  router.push('/login');
};
</script>

<style lang="scss" scoped>
.admin-login-container {
  width: 100vw;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  top: 0;
  left: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  overflow: hidden;
  z-index: 1000;
  
  .login-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse"><path d="M 10 0 L 0 0 0 10" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="0.5"/></pattern></defs><rect width="100" height="100" fill="url(%23grid)"/></svg>');
    opacity: 0.3;
  }
  
  .login-content {
    position: relative;
    z-index: 1;
    width: 100%;
    max-width: 400px;
  }
  
  .login-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 40px 32px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
  }
  
  .login-header {
    text-align: center;
    margin-bottom: 32px;
    
    .admin-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: linear-gradient(135deg, #ff6b6b, #ee5a24);
      color: white;
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 0.9rem;
      font-weight: 600;
      margin-bottom: 16px;
      
      .el-icon {
        font-size: 1.1rem;
      }
    }
    
    h1 {
      font-size: 1.8rem;
      font-weight: 700;
      color: #2c3e50;
      margin-bottom: 8px;
      background: linear-gradient(135deg, #667eea, #764ba2);
      background-clip: text;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      color: transparent; /* 标准属性，用于兼容性 */
    }
    
    p {
      color: #7f8c8d;
      font-size: 1rem;
      margin: 0;
    }
  }
  
  .login-form {
    .el-form-item {
      margin-bottom: 20px;
    }
    
    .el-input {
      :deep(.el-input__wrapper) {
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        border: 1px solid #e1e8ed;
        transition: all 0.3s ease;
        
        &:hover {
          border-color: #667eea;
        }
        
        &.is-focus {
          border-color: #667eea;
          box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
        }
      }
      
      :deep(.el-input__inner) {
        height: 48px;
        font-size: 1rem;
      }
    }
    

    
    .login-btn {
      width: 100%;
      height: 48px;
      font-size: 1rem;
      font-weight: 600;
      border-radius: 12px;
      background: linear-gradient(135deg, #667eea, #764ba2);
      border: none;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
  }
  
  .login-footer {
    margin-top: 24px;
    
    .el-divider {
      margin: 16px 0;
      
      :deep(.el-divider__text) {
        color: #95a5a6;
        font-size: 0.875rem;
      }
    }
    
    .other-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .el-link {
        font-size: 0.875rem;
      }
    }
  }
}

@media (max-width: 480px) {
  .admin-login-container {
    padding: 16px;
    
    .login-card {
      padding: 32px 24px;
    }
    
    .login-header {
      h1 {
        font-size: 1.5rem;
      }
    }
  }
}
</style> 