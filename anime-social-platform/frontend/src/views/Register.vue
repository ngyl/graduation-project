<template>
  <app-layout :showFooter="false">
    <div class="register-page">
      <div class="register-box">
        <div class="register-header">
          <h2>动漫社交平台</h2>
          <p>创建一个账号，开始您的动漫社交之旅</p>
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
  </app-layout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import AppLayout from '@/components/AppLayout.vue'

const router = useRouter()
const userStore = useUserStore()
const registerFormRef = ref()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: Function) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

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
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.registerUser(
          registerForm.username, 
          registerForm.password,
          registerForm.confirmPassword
        )
        
        if (success) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error('注册失败，请稍后重试')
        }
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.register-box {
  width: 100%;
  max-width: 450px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
  padding: 40px;
  text-align: center;
  transition: transform 0.3s ease;
}

.register-box:hover {
  transform: translateY(-5px);
}

.register-header {
  margin-bottom: 30px;
}

.register-header h2 {
  color: #409eff;
  font-size: 28px;
  margin-bottom: 10px;
}

.register-header p {
  color: #666;
  font-size: 16px;
}

.register-form {
  text-align: left;
}

.custom-input {
  height: 50px;
}

.register-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  margin-top: 10px;
}

.form-footer {
  margin-top: 20px;
  text-align: center;
}

.login-link {
  color: #409eff;
  text-decoration: none;
  transition: all 0.3s ease;
}

.login-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .register-box {
    padding: 30px 20px;
  }
}
</style> 