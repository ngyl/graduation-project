<template>
  <div class="register-container">
    <div class="back-button">
      <el-button 
        type="text" 
        @click="router.push('/')"
        class="back-home"
      >
        <el-icon><ArrowLeft /></el-icon>
        返回主页
      </el-button>
    </div>
    <div class="register-content">
      <div class="register-left">
        <div class="welcome-image"></div>
        <h3>加入动漫社交平台</h3>
        <p>创建账号，开启你的动漫社交之旅</p>
      </div>
      <div class="register-box">
        <div class="register-header">
          <h2>动漫社交平台</h2>
          <p>创建新账号，开启您的动漫之旅</p>
        </div>
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form">
          <el-form-item prop="username">
            <el-input 
              v-model="registerForm.username" 
              placeholder="用户名" 
              prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="密码" 
              prefix-icon="Lock" 
              show-password
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="确认密码" 
              prefix-icon="Lock" 
              show-password
              class="custom-input"
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleRegister" 
              :loading="loading" 
              class="register-button"
            >
              注册
            </el-button>
          </el-form-item>
          <div class="form-footer">
            <router-link to="/login" class="login-link">
              已有账号？立即登录
            </router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { ArrowLeft } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const registerFormRef = ref();
const loading = ref(false);

const registerForm = reactive({
    username: '',
    password: '',
    confirmPassword: ''
});

const validatePass2 = (_: any, value: string, callback: any) => {
    if (value === '') {
        callback(new Error('请再次输入密码'));
    } else if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致!'));
    } else {
        callback();
    }
};

const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validatePass2, trigger: 'blur' }
    ]
};

const handleRegister = async () => {
    if (!registerFormRef.value) return;
    
    await registerFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
            loading.value = true;
            try {
                const success = await userStore.registerUser(
                    registerForm.username,
                    registerForm.password,
                    registerForm.confirmPassword
                );
                if (success) {
                    ElMessage.success('注册成功');
                    router.push('/login');
                } else {
                    ElMessage.error('注册失败，请稍后重试');
                }
            } catch (error: any) {
                ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试');
            } finally {
                loading.value = false;
            }
        }
    });
};
</script>

<style scoped>
.register-container {
    min-height: 100vh;
    background: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%);
    padding: 20px;
    position: relative;
    overflow: hidden;
}

.back-button {
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 10;
}

.back-home {
    display: flex;
    align-items: center;
    gap: 5px;
    color: #fff;
    font-size: 16px;
    padding: 10px 20px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
}

.back-home:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: translateX(-5px);
}

.register-content {
    max-width: 1200px;
    margin: 40px auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 40px;
    padding: 20px;
}

.register-left {
    flex: 1;
    display: none;
    text-align: center;
    color: #fff;
}

.welcome-image {
    width: 100%;
    height: 300px;
    background: url('@/assets/images/welcome.png') center/contain no-repeat;
    margin-bottom: 30px;
}

.register-left h3 {
    font-size: 28px;
    margin-bottom: 15px;
    font-weight: 600;
}

.register-left p {
    font-size: 16px;
    line-height: 1.6;
    opacity: 0.9;
}

.register-box {
    width: 100%;
    max-width: 400px;
    padding: 40px;
    background-color: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
    backdrop-filter: blur(20px);
    position: relative;
    z-index: 1;
    animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.register-header {
    text-align: center;
    margin-bottom: 40px;
}

.register-header h2 {
    color: #2c3e50;
    font-size: 32px;
    margin-bottom: 15px;
    font-weight: 700;
    background: linear-gradient(120deg, #4a90e2, #8e44ad);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
}

.register-header p {
    color: #666;
    font-size: 16px;
    line-height: 1.6;
}

.register-form {
    margin-top: 30px;
}

.custom-input {
    margin-bottom: 20px;
}

.custom-input :deep(.el-input__wrapper) {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    border-radius: 12px;
    padding: 12px 20px;
    background: rgba(255, 255, 255, 0.8);
    transition: all 0.3s ease;
}

.custom-input :deep(.el-input__wrapper:hover) {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
}

.custom-input :deep(.el-input__inner) {
    font-size: 15px;
}

.register-button {
    width: 100%;
    height: 48px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(120deg, #4a90e2, #8e44ad);
    border: none;
    transition: all 0.3s ease;
    margin-top: 10px;
}

.register-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(74, 144, 226, 0.3);
}

.register-button:active {
    transform: translateY(0);
}

.form-footer {
    text-align: center;
    margin-top: 30px;
}

.login-link {
    color: #4a90e2;
    text-decoration: none;
    font-size: 15px;
    font-weight: 500;
    transition: all 0.3s ease;
    position: relative;
}

.login-link::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 100%;
    height: 2px;
    background: linear-gradient(120deg, #4a90e2, #8e44ad);
    transform: scaleX(0);
    transition: transform 0.3s ease;
}

.login-link:hover::after {
    transform: scaleX(1);
}

/* 响应式设计 */
@media (min-width: 1024px) {
    .register-left {
        display: block;
    }
    
    .register-box {
        flex: 0 0 400px;
    }
}

@media (max-width: 768px) {
    .register-content {
        margin: 20px auto;
        padding: 10px;
    }

    .register-box {
        padding: 30px 25px;
        margin: 0 auto;
    }

    .back-home {
        font-size: 14px;
        padding: 8px 15px;
    }
}

@media (max-width: 480px) {
    .register-box {
        padding: 25px 20px;
    }

    .back-button {
        top: 10px;
        left: 10px;
    }
}
</style> 