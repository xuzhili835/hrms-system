<template>
  <div class="login-container">
    <div class="login-background"></div>
    <div class="login-center">
      <div class="login-card">
        <div class="login-header">
          <div class="employee-badge">
            <el-icon><Avatar /></el-icon>
            <span>员工登录</span>
          </div>
          <h1 class="main-title">人力资源管理系统</h1>
          <p class="login-desc">员工工作台</p>
        </div>
        
        <el-form ref="loginForm" :model="form" :rules="rules" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名/邮箱"
              size="large"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              class="login-btn"
              @click="handleLogin"
              :loading="loading"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <div class="admin-login-section">
            <el-divider></el-divider>
            <el-button 
              type="warning" 
              size="default"
              class="admin-login-btn"
              @click.stop="goToAdminLogin"
              @mouseenter="onButtonMouseEnter"
              @mouseleave="onButtonMouseLeave"
            >
              <el-icon><Setting /></el-icon>
              管理员入口
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { User, Lock, Avatar, Setting } from '@element-plus/icons-vue';
import { useAuthStore } from '@/store/auth';
import { useRouter } from 'vue-router';

const router = useRouter();
const authStore = useAuthStore();
const loginForm = ref();

const form = reactive({
  username: '',
  password: '',
});

const rules = reactive({
  username: [{ required: true, message: '请输入员工账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
});

const loading = ref(false);

const handleLogin = async () => {
  if (!loginForm.value) return;

  loading.value = true;
  await loginForm.value.validate(async (valid) => {
    if (valid) {
      const success = await authStore.login(form);
      if (success) {
        ElMessage.success('登录成功！');
      } else {
        ElMessage.error(authStore.error || '登录失败，请检查您的凭据');
      }
    } else {
      ElMessage.error('请检查输入项是否正确');
    }
    loading.value = false;
  });
};

const goToAdminLogin = () => {
  console.log('管理员入口按钮被点击');
  router.push('/admin-login');
};

// 鼠标事件处理
const onButtonMouseEnter = (event) => {
  console.log('鼠标进入管理员按钮');
  event.target.style.cursor = 'pointer';
};

const onButtonMouseLeave = (event) => {
  console.log('鼠标离开管理员按钮');
};
</script>

<style lang="scss" scoped>
.login-container {
  width: 100vw;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  top: 0;
  left: 0;
  overflow: hidden;
  padding: 24px 0;
  z-index: 1000;
  
  .login-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, #1e88e5, #0d47a1);
    opacity: 0.1;
    z-index: 0;
  }
  
  .login-center {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px; /* 添加左右内边距 */
  }
  
  .login-card {
    width: 100%;
    max-width: 480px;
    min-width: 320px;
    margin: auto;
    background: #fff;
    padding: 40px 32px 32px 32px;
    border-radius: 16px;
    box-shadow: 0 12px 40px rgba(64, 158, 255, 0.15);
    display: flex;
    flex-direction: column;
    align-items: center;
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 15px 50px rgba(64, 158, 255, 0.25);
      transform: translateY(-5px);
    }
  }
  
  .login-header {
    text-align: center;
    margin-bottom: 30px;
    width: 100%;
    
    .employee-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: linear-gradient(135deg, #00b894, #00a085);
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
  }
  
  .logo {
    width: 80px;
    height: 80px;
    margin-bottom: 15px;
    border-radius: 50%;
    background: #f0f7ff;
    padding: 12px;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
  }
  
  .main-title {
    display: block;
    width: 100%;
    text-align: center;
    font-size: 1.8rem;
    margin-bottom: 8px;
    color: #1f2d3d;
    font-weight: 700;
    background: linear-gradient(90deg, #1e88e5, #0d47a1);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    color: transparent; /* 标准属性，用于兼容性 */
  }
  
  .login-desc {
    color: #5a6c85;
    font-size: 1rem;
    margin-bottom: 25px;
    font-weight: 500;
  }
  
  .login-header h2 {
    margin: 20px 0 8px 0;
    color: #1f2d3d;
    font-weight: 600;
    font-size: 1.5rem;
  }
  
  .login-header p {
    margin: 0;
    color: #6b7c95;
    font-size: 0.95rem;
  }
  
  .login-form {
    width: 100%;
    margin-bottom: 20px;
    
    .el-input {
      margin-bottom: 20px;
      
      &__inner {
        height: 48px;
        border-radius: 8px;
        padding-left: 40px;
      }
      
      &__prefix {
        left: 12px;
        font-size: 18px;
        color: #5a6c85;
      }
    }
  }
  

  
  .login-btn {
    width: 100%;
    height: 50px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 8px;
    background: linear-gradient(90deg, #1e88e5, #0d47a1);
    border: none;
    box-shadow: 0 4px 15px rgba(30, 136, 229, 0.3);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 6px 20px rgba(30, 136, 229, 0.4);
    }
    
    &:active {
      transform: translateY(1px);
    }
  }
  
  .login-footer {
    width: 100%;
    margin-top: 20px;
    
    .admin-login-section {
      text-align: center;
      
    .el-divider {
        margin: 25px 0 25px 0;
      border-color: #e0e6ef;
      }
      
      .admin-login-btn {
        background: linear-gradient(135deg, #ffab91, #ff8a65) !important;
        border: none !important;
        color: white !important;
        padding: 12px 28px !important;
        border-radius: 25px !important;
        font-size: 0.9rem !important;
        font-weight: 500 !important;
        box-shadow: 0 4px 15px rgba(255, 138, 101, 0.25) !important;
        transition: all 0.3s ease !important;
        min-width: 140px !important;
        position: relative !important;
        z-index: 999 !important;
        cursor: pointer !important;
        pointer-events: auto !important;
        user-select: none !important;
        
        // 确保按钮可以接收所有事件
        &, &:before, &:after, & * {
          pointer-events: auto !important;
        }
        
        &:hover {
          transform: translateY(-2px) !important;
          box-shadow: 0 6px 20px rgba(255, 138, 101, 0.35) !important;
          background: linear-gradient(135deg, #ff8a65, #ff7043) !important;
          cursor: pointer !important;
        }
        
        &:active {
          transform: translateY(0) !important;
          background: linear-gradient(135deg, #ff7043, #ff5722) !important;
        }
        
        &:focus {
          outline: none !important;
          box-shadow: 0 6px 20px rgba(255, 138, 101, 0.35) !important;
        }
        
        // 确保Element Plus的按钮样式不会覆盖
        &.el-button {
          background: linear-gradient(135deg, #ffab91, #ff8a65) !important;
          border: none !important;
          color: white !important;
          
          &:hover {
            background: linear-gradient(135deg, #ff8a65, #ff7043) !important;
            border: none !important;
            color: white !important;
          }
          
          &:focus {
            background: linear-gradient(135deg, #ffab91, #ff8a65) !important;
            border: none !important;
            color: white !important;
          }
        }
        
        .el-icon {
          margin-right: 6px !important;
          font-size: 1rem !important;
          pointer-events: none !important;
        }
      }
    }
  }
}

@media (max-width: 600px) {
  .login-card {
    padding: 24px 8px;
    min-width: 0;
  }
  
  .login-header h1 {
    font-size: 1.5rem;
  }
  
  .login-header h2 {
    font-size: 1.3rem;
  }
  
  .logo {
    width: 70px;
    height: 70px;
  }
}
</style>