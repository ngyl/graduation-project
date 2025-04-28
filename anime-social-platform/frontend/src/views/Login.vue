<template>
  <app-layout :showFooter="false">
    <div class="login-page">
      <div class="login-box">
        <div class="login-header">
          <h2>动漫社交平台</h2>
          <p>欢迎回来！请登录您的账号</p>
        </div>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="用户名" 
              prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="密码" 
              prefix-icon="Lock" 
              show-password
              class="custom-input"
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleLogin" 
              :loading="loading" 
              class="login-button"
            >
              登录
            </el-button>
          </el-form-item>
          <div class="form-footer">
            <router-link to="/register" class="register-link">
              还没有账号？立即注册
            </router-link>
          </div>
        </el-form>
      </div>
    </div>
  </app-layout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import AppLayout from '@/components/AppLayout.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)
const redirectPath = ref('/')

// 获取重定向地址
onMounted(() => {
  const redirect = route.query.redirect as string
  if (redirect) {
    redirectPath.value = redirect
  }

  // 如果已经登录，直接跳转
  if (userStore.isLoggedIn) {
    router.push(redirectPath.value)
  }
})

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.loginUser(loginForm.username, loginForm.password)
        if (success) {
          ElMessage.success('登录成功')
          
          // 如果有重定向地址，登录后跳转到该地址
          router.push(redirectPath.value)
        } else {
          ElMessage.error('登录失败，请检查用户名和密码')
        }
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '登录失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 450px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
  padding: 40px;
  text-align: center;
  transition: transform 0.3s ease;
}

.login-box:hover {
  transform: translateY(-5px);
}

.login-header {
  margin-bottom: 30px;
}

.login-header h2 {
  color: #409eff;
  font-size: 28px;
  margin-bottom: 10px;
}

.login-header p {
  color: #666;
  font-size: 16px;
}

.login-form {
  text-align: left;
}

.custom-input {
  height: 50px;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  margin-top: 10px;
}

.form-footer {
  margin-top: 20px;
  text-align: center;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  transition: all 0.3s ease;
}

.register-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .login-box {
    padding: 30px 20px;
  }
}
</style> 