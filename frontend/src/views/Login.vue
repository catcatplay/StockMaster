<template>
  <div class="login-wrapper">
    <div class="login-left">
      <div class="brand-content">
        <div class="logo-circle">
          <el-icon :size="40" color="#4f46e5"><Box /></el-icon>
        </div>
        <h1 class="brand-title">Stock Master</h1>
        <p class="brand-subtitle">专业的企业级库存管理系统</p>
        <div class="brand-features">
          <div class="feature-item">
            <el-icon><Check /></el-icon> <span>实时库存监控</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon> <span>智能出入库管理</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon> <span>多维度数据报表</span>
          </div>
        </div>
      </div>
      <!-- 装饰背景圆 -->
      <div class="circle-1"></div>
      <div class="circle-2"></div>
    </div>
    
    <div class="login-right">
      <div class="login-form-container">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p class="sub-text">请登录您的账号以继续</p>
        </div>
        
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form" size="large">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名"
              :prefix-icon="User">
            </el-input>
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin">
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              class="login-btn"
              :loading="loading"
              @click="handleLogin">
              立即登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="form-footer">
          <p>© 2026 Stock Master. All Rights Reserved.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Box, Check } from '@element-plus/icons-vue'
import { login } from '../api/auth'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        if (res.code === 200) {
          localStorage.setItem('token', res.data.token)
          localStorage.setItem('userInfo', JSON.stringify(res.data))
          
          ElMessage.success('登录成功')
          
          const roleCode = res.data.roleName
          if (roleCode === '入库员') {
            router.push('/device/inbound')
          } else if (roleCode === '出库员' || roleCode === '销售') {
            router.push('/device/outbound')
          } else {
            router.push('/device/goods')
          }
        } else {
          ElMessage.error(res.msg || '登录失败')
        }
      } catch (error) {
        // Error handled globally or by interceptor ideally
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-wrapper {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: #fff;
}

.login-left {
  width: 45%;
  background: linear-gradient(135deg, #4f46e5 0%, #3730a3 100%);
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 80px;
  color: white;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 10;
}

.logo-circle {
  width: 64px;
  height: 64px;
  background: white;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.brand-title {
  font-size: 3rem;
  font-weight: 800;
  margin: 0 0 16px 0;
  letter-spacing: -0.02em;
}

.brand-subtitle {
  font-size: 1.25rem;
  opacity: 0.9;
  margin-bottom: 48px;
  font-weight: 300;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.1rem;
  opacity: 0.8;
}

.circle-1 {
  position: absolute;
  top: -10%;
  right: -10%;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 70%);
}

.circle-2 {
  position: absolute;
  bottom: -10%;
  left: -10%;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 60%);
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
}

.login-form-container {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.sub-text {
  color: #6b7280;
  font-size: 1rem;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 1rem;
  margin-top: 8px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.form-footer {
  margin-top: 32px;
  text-align: center;
  color: #9ca3af;
  font-size: 0.875rem;
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    background: #f3f4f6;
  }
  
  .login-form-container {
    box-shadow: none;
    background: transparent;
  }
}
</style>
