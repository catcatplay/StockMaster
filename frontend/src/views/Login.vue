<template>
  <div class="login-wrapper">
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>

    <div class="login-left">
      <div class="grid-overlay"></div>
      <div class="glow-effect"></div>

      <div class="brand-content">
        <div class="logo-container">
          <div class="logo-glow"></div>
          <el-icon :size="36" color="#f7faff"><Box /></el-icon>
        </div>

        <h1 class="brand-title">Stock Master</h1>
        <p class="brand-subtitle">企业级智能库存管理平台</p>

        <div class="brand-features">
          <div v-for="(feature, index) in features" :key="index" class="feature-item">
            <div class="feature-icon">
              <el-icon><Check /></el-icon>
            </div>
            <span>{{ feature }}</span>
          </div>
        </div>

        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-value">99.9%</div>
            <div class="stat-label">系统可用率</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">10ms</div>
            <div class="stat-label">响应时间</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">24/7</div>
            <div class="stat-label">技术支持</div>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="glass-card">
        <div class="card-glow"></div>

        <div class="form-header">
          <h2>欢迎回来</h2>
          <p class="sub-text">登录您的账号以继续使用系统</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          size="large"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <div class="input-wrapper">
              <el-input
                v-model="loginForm.username"
                placeholder="用户名"
                :prefix-icon="User"
                class="glass-input"
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-wrapper">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                :prefix-icon="Lock"
                show-password
                class="glass-input"
                @keyup.enter="handleLogin"
              />
            </div>
          </el-form-item>

          <el-form-item class="submit-item">
            <button type="button" class="gradient-btn" :disabled="loading" @click="handleLogin">
              <span class="btn-glow"></span>
              <span v-if="loading" class="btn-spinner" aria-hidden="true"></span>
              <span class="btn-text">{{ loading ? '登录中...' : '立即登录' }}</span>
            </button>
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
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Box, Check } from '@element-plus/icons-vue'
import { login } from '../api/auth'

const router = useRouter()
const loginFormRef = ref(null)
const particleCanvas = ref(null)
const loading = ref(false)

const features = [
  '实时库存监控与预警',
  '智能出入库管理',
  '多维度数据分析'
]

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

let animationId = null
let particles = []
let glowSpots = []
let mouseX = 0
let mouseY = 0
let resizeHandler = null
let mouseMoveHandler = null

class Particle {
  constructor(canvas) {
    this.canvas = canvas
    this.reset()
  }

  reset() {
    this.x = Math.random() * this.canvas.width
    this.y = Math.random() * this.canvas.height
    this.vx = (Math.random() - 0.5) * 0.12
    this.vy = (Math.random() - 0.5) * 0.12
    this.radius = Math.random() * 0.9 + 0.45
    this.opacity = Math.random() * 0.18 + 0.05
    this.baseOpacity = this.opacity
  }

  update() {
    this.x += this.vx
    this.y += this.vy

    if (this.x < 0 || this.x > this.canvas.width) this.vx *= -1
    if (this.y < 0 || this.y > this.canvas.height) this.vy *= -1

    const dx = mouseX - this.x
    const dy = mouseY - this.y
    const distance = Math.sqrt(dx * dx + dy * dy)

    if (distance < 140) {
      const force = (140 - distance) / 140
      this.opacity = this.baseOpacity + force * 0.08
    } else {
      this.opacity = this.baseOpacity
    }
  }

  draw(ctx) {
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(226, 233, 255, ${this.opacity})`
    ctx.fill()

    if (this.radius > 0.95) {
      ctx.beginPath()
      ctx.arc(this.x, this.y, this.radius * 1.8, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(214, 225, 255, ${this.opacity * 0.08})`
      ctx.fill()
    }
  }
}

const initParticles = () => {
  if (!particleCanvas.value) return

  const canvas = particleCanvas.value
  const ctx = canvas.getContext('2d')

  resizeHandler = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }

  mouseMoveHandler = (event) => {
    mouseX = event.clientX
    mouseY = event.clientY
  }

  resizeHandler()
  window.addEventListener('resize', resizeHandler)
  window.addEventListener('mousemove', mouseMoveHandler)

  const particleCount = Math.floor((canvas.width * canvas.height) / 24000)
  particles = Array.from({ length: particleCount }, () => new Particle(canvas))

  glowSpots = Array.from({ length: 3 }, () => ({
    x: Math.random() * canvas.width,
    y: Math.random() * canvas.height,
    radius: Math.random() * 180 + 120,
    vx: (Math.random() - 0.5) * 0.05,
    vy: (Math.random() - 0.5) * 0.05
  }))

  const animate = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    glowSpots.forEach((spot) => {
      spot.x += spot.vx
      spot.y += spot.vy

      if (spot.x < -spot.radius || spot.x > canvas.width + spot.radius) spot.vx *= -1
      if (spot.y < -spot.radius || spot.y > canvas.height + spot.radius) spot.vy *= -1

      const gradient = ctx.createRadialGradient(spot.x, spot.y, 0, spot.x, spot.y, spot.radius)
      gradient.addColorStop(0, 'rgba(193, 213, 255, 0.045)')
      gradient.addColorStop(0.45, 'rgba(205, 215, 255, 0.016)')
      gradient.addColorStop(1, 'rgba(255, 255, 255, 0)')

      ctx.fillStyle = gradient
      ctx.fillRect(0, 0, canvas.width, canvas.height)
    })

    const cellSize = 96
    ctx.strokeStyle = 'rgba(210, 223, 255, 0.028)'
    ctx.lineWidth = 1

    for (let x = 0; x <= canvas.width; x += cellSize) {
      ctx.beginPath()
      ctx.moveTo(x + 0.5, 0)
      ctx.lineTo(x + 0.5, canvas.height)
      ctx.stroke()
    }

    for (let y = 0; y <= canvas.height; y += cellSize) {
      ctx.beginPath()
      ctx.moveTo(0, y + 0.5)
      ctx.lineTo(canvas.width, y + 0.5)
      ctx.stroke()
    }

    particles.forEach((particle) => {
      particle.update()
      particle.draw(ctx)
    })

    particles.forEach((p1, index) => {
      particles.slice(index + 1).forEach((p2) => {
        const dx = p1.x - p2.x
        const dy = p1.y - p2.y
        const distance = Math.sqrt(dx * dx + dy * dy)
        const aligned = Math.abs(dx) < 26 || Math.abs(dy) < 26

        if (distance < 92 && aligned) {
          ctx.beginPath()
          ctx.moveTo(p1.x, p1.y)
          ctx.lineTo(p2.x, p2.y)
          ctx.strokeStyle = `rgba(205, 220, 255, ${0.04 * (1 - distance / 92)})`
          ctx.lineWidth = 0.5
          ctx.stroke()
        }
      })
    })

    animationId = requestAnimationFrame(animate)
  }

  animate()
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

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
      // 错误由拦截器或全局消息处理
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  initParticles()
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
  if (mouseMoveHandler) window.removeEventListener('mousemove', mouseMoveHandler)
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.login-wrapper {
  --text-strong: rgba(247, 250, 255, 0.98);
  --text-primary: rgba(232, 238, 255, 0.9);
  --text-secondary: rgba(221, 229, 247, 0.79);
  --text-tertiary: rgba(203, 212, 236, 0.61);
  --text-placeholder: rgba(196, 205, 229, 0.56);
  --radius-panel: 28px;
  --radius-surface: 18px;
  --radius-control: 16px;
  --glass-fill:
    radial-gradient(circle at 50% 0%, rgba(255, 255, 255, 0.12), transparent 52%),
    linear-gradient(155deg, rgba(245, 248, 255, 0.16) 0%, rgba(219, 228, 249, 0.082) 56%, rgba(204, 214, 240, 0.058) 100%);
  --glass-fill-soft:
    radial-gradient(circle at 50% 0%, rgba(255, 255, 255, 0.085), transparent 58%),
    linear-gradient(150deg, rgba(244, 248, 255, 0.13), rgba(214, 224, 250, 0.06));
  --glass-stroke: rgba(236, 242, 255, 0.24);
  --glass-stroke-strong: rgba(245, 248, 255, 0.34);
  --glass-shadow: 0 28px 76px rgba(11, 19, 46, 0.24);
  --glass-shadow-soft: 0 18px 38px rgba(11, 19, 46, 0.14);
  --surface-highlight: rgba(255, 255, 255, 0.42);
  --surface-inner-stroke: rgba(255, 255, 255, 0.055);
  --field-fill:
    radial-gradient(circle at 50% 0%, rgba(255, 255, 255, 0.068), transparent 60%),
    linear-gradient(180deg, rgba(244, 247, 255, 0.104), rgba(217, 226, 250, 0.058));
  --field-stroke: rgba(233, 239, 255, 0.2);
  --field-stroke-focus: rgba(245, 248, 255, 0.4);
  --field-shadow: 0 12px 30px rgba(12, 20, 48, 0.09);
  --error-soft: rgba(255, 171, 163, 0.92);
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 24%, rgba(140, 168, 255, 0.16), transparent 32%),
    radial-gradient(circle at 82% 20%, rgba(210, 220, 255, 0.09), transparent 28%),
    radial-gradient(circle at 70% 78%, rgba(133, 146, 218, 0.12), transparent 36%),
    linear-gradient(135deg, #4252b2 0%, #5260bd 26%, #5d67b6 58%, #6667ab 100%);
  position: relative;
}

.login-wrapper::before,
.login-wrapper::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.login-wrapper::before {
  background:
    linear-gradient(120deg, rgba(255, 255, 255, 0.035), transparent 24%, transparent 74%, rgba(255, 255, 255, 0.025)),
    linear-gradient(180deg, rgba(7, 11, 29, 0.08), transparent 28%, transparent 72%, rgba(255, 255, 255, 0.03));
  z-index: 0;
}

.login-wrapper::after {
  background: radial-gradient(circle at 50% 50%, transparent 0%, rgba(10, 14, 34, 0.14) 100%);
  z-index: 0;
}

.particle-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
  opacity: 0.7;
}

.login-left {
  width: 50%;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 80px;
  color: white;
  z-index: 2;
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(233, 239, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(233, 239, 255, 0.04) 1px, transparent 1px);
  background-size: 56px 56px;
  opacity: 0.32;
  mask-image: radial-gradient(ellipse 80% 100% at 40% 50%, black 40%, transparent 100%);
}

.glow-effect {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background:
    radial-gradient(circle at 35% 50%, rgba(235, 241, 255, 0.045) 0%, transparent 34%),
    radial-gradient(circle at 62% 38%, rgba(185, 204, 255, 0.03) 0%, transparent 30%);
  animation: rotate 50s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.brand-content {
  position: relative;
  z-index: 10;
}

.logo-container {
  position: relative;
  width: 56px;
  height: 56px;
  background: linear-gradient(145deg, rgba(246, 249, 255, 0.16), rgba(214, 224, 251, 0.08));
  backdrop-filter: blur(20px) saturate(140%);
  border: 1px solid var(--glass-stroke);
  border-radius: var(--radius-control);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
  box-shadow:
    0 14px 36px rgba(10, 16, 36, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.34),
    inset 0 -8px 18px rgba(255, 255, 255, 0.04);
}

.logo-glow {
  position: absolute;
  width: 120%;
  height: 120%;
  background: radial-gradient(circle, rgba(225, 235, 255, 0.2) 0%, transparent 72%);
  border-radius: 14px;
  animation: pulse 4s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.3;
    transform: scale(1);
  }

  50% {
    opacity: 0.55;
    transform: scale(1.04);
  }
}

.brand-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin: 0 0 16px;
  letter-spacing: -0.03em;
  color: var(--text-strong);
  text-shadow: 0 6px 28px rgba(7, 12, 28, 0.2);
}

.brand-subtitle {
  font-size: 1.125rem;
  color: var(--text-secondary);
  margin-bottom: 56px;
  font-weight: 400;
  letter-spacing: 0.02em;
  line-height: 1.7;
  max-width: 540px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 56px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.9375rem;
  color: var(--text-primary);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 4px 0;
  font-weight: 500;
}

.feature-item:hover {
  color: var(--text-strong);
  transform: translateX(6px);
}

.feature-icon {
  width: 22px;
  height: 22px;
  background: linear-gradient(145deg, rgba(245, 248, 255, 0.13), rgba(214, 224, 251, 0.05));
  backdrop-filter: blur(10px);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(240, 245, 255, 0.2);
  flex-shrink: 0;
  box-shadow: 0 8px 18px rgba(12, 19, 43, 0.12), inset 0 1px 0 rgba(255, 255, 255, 0.18);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 24px;
}

.stat-item {
  position: relative;
  overflow: hidden;
  text-align: center;
  padding: 22px 16px 18px;
  background: var(--glass-fill-soft);
  backdrop-filter: blur(26px) saturate(135%);
  border: 1px solid var(--glass-stroke);
  border-radius: var(--radius-surface);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    var(--glass-shadow-soft),
    inset 0 1px 0 rgba(255, 255, 255, 0.26),
    inset 0 -12px 22px rgba(255, 255, 255, 0.026);
}

.stat-item::before,
.stat-item::after {
  content: '';
  position: absolute;
  pointer-events: none;
}

.stat-item::before {
  inset: 1px;
  border-radius: calc(var(--radius-surface) - 1px);
  border: 1px solid var(--surface-inner-stroke);
  mask-image: linear-gradient(to bottom, black, transparent 82%);
}

.stat-item::after {
  top: 0;
  left: 18%;
  width: 64%;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--surface-highlight), transparent);
  opacity: 0.5;
}

.stat-item:hover {
  border-color: var(--glass-stroke-strong);
  transform: translateY(-2px);
  box-shadow:
    0 22px 44px rgba(10, 16, 37, 0.17),
    inset 0 1px 0 rgba(255, 255, 255, 0.28);
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 10px;
  letter-spacing: -0.03em;
  color: var(--text-strong);
  line-height: 1;
}

.stat-label {
  font-size: 0.8rem;
  color: rgba(201, 210, 233, 0.68);
  font-weight: 500;
  letter-spacing: 0.02em;
  line-height: 1.45;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  padding: 40px;
}

.glass-card {
  width: 100%;
  max-width: 440px;
  padding: 52px 44px;
  background: var(--glass-fill);
  backdrop-filter: blur(32px) saturate(142%);
  -webkit-backdrop-filter: blur(32px) saturate(142%);
  border: 1px solid var(--glass-stroke);
  border-radius: var(--radius-panel);
  box-shadow:
    var(--glass-shadow),
    0 12px 34px rgba(44, 58, 118, 0.07),
    inset 0 1px 0 rgba(255, 255, 255, 0.4),
    inset 0 -20px 30px rgba(255, 255, 255, 0.028);
  position: relative;
  overflow: hidden;
}

.glass-card::before,
.glass-card::after {
  content: '';
  position: absolute;
  pointer-events: none;
}

.glass-card::before {
  inset: 1px;
  border-radius: calc(var(--radius-panel) - 1px);
  border: 1px solid var(--surface-inner-stroke);
  mask-image: linear-gradient(to bottom, black, transparent 80%);
}

.glass-card::after {
  top: 0;
  left: 12%;
  width: 76%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
  opacity: 0.74;
}

.card-glow {
  position: absolute;
  inset: -34%;
  background:
    radial-gradient(circle at 28% 18%, rgba(244, 247, 255, 0.09) 0%, transparent 30%),
    radial-gradient(circle at 50% 42%, rgba(220, 229, 252, 0.048) 0%, transparent 38%),
    radial-gradient(circle at 74% 78%, rgba(202, 214, 249, 0.032) 0%, transparent 34%);
  animation: rotate 30s linear infinite;
  pointer-events: none;
  opacity: 0.58;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
}

.form-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-strong);
  margin: 0 0 10px;
  letter-spacing: -0.025em;
  text-shadow: 0 4px 18px rgba(7, 12, 28, 0.12);
}

.sub-text {
  color: var(--text-secondary);
  font-size: 0.9375rem;
  font-weight: 500;
  letter-spacing: 0.02em;
  line-height: 1.65;
  max-width: 260px;
  margin: 0 auto;
}

.login-form {
  position: relative;
  z-index: 1;
}

.input-wrapper {
  position: relative;
}

:deep(.glass-input) {
  background: var(--field-fill) !important;
  backdrop-filter: blur(18px) saturate(135%);
  -webkit-backdrop-filter: blur(18px) saturate(135%);
  border: 1px solid var(--field-stroke) !important;
  border-radius: var(--radius-control);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    var(--field-shadow),
    inset 0 1px 0 rgba(255, 255, 255, 0.18),
    inset 0 -10px 18px rgba(255, 255, 255, 0.026);
}

:deep(.glass-input .el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  min-height: 54px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  border-radius: inherit;
}

:deep(.glass-input input) {
  background: transparent !important;
  color: var(--text-primary) !important;
  font-weight: 500;
  font-size: 0.95rem;
  letter-spacing: 0.015em;
  line-height: 54px;
}

:deep(.glass-input input::placeholder) {
  color: var(--text-placeholder);
  font-weight: 400;
}

:deep(.glass-input:hover) {
  border-color: rgba(243, 247, 255, 0.28) !important;
  box-shadow:
    0 14px 32px rgba(11, 19, 46, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.22);
}

:deep(.glass-input.is-focus) {
  border-color: var(--field-stroke-focus) !important;
  box-shadow:
    0 0 0 1px rgba(243, 247, 255, 0.2),
    0 12px 34px rgba(11, 19, 46, 0.11),
    0 0 0 4px rgba(205, 221, 255, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.24);
}

:deep(.el-input__prefix) {
  color: rgba(219, 228, 249, 0.68);
  font-size: 17px;
  margin-right: 10px;
  display: flex;
  align-items: center;
}

:deep(.el-input__prefix-inner) {
  display: flex;
  align-items: center;
}

:deep(.el-input__suffix) {
  color: rgba(229, 236, 255, 0.82);
  display: flex;
  align-items: center;
}

:deep(.el-input__suffix:hover) {
  color: rgba(248, 250, 255, 0.96);
}

:deep(.el-input__icon) {
  font-size: 17px;
}

:deep(.el-input__suffix-inner) {
  display: flex;
  align-items: center;
  gap: 6px;
}

:deep(.el-input .el-input__clear),
:deep(.el-input .el-input__password) {
  width: 26px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  color: rgba(229, 236, 255, 0.82);
  background: rgba(255, 255, 255, 0.04);
  transition: color 0.2s ease, background-color 0.2s ease, opacity 0.2s ease;
}

:deep(.el-input .el-input__clear:hover),
:deep(.el-input .el-input__password:hover) {
  color: rgba(248, 250, 255, 0.98);
  background: rgba(255, 255, 255, 0.08);
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__content) {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

:deep(.el-form-item.is-error .glass-input) {
  border-color: rgba(255, 182, 171, 0.34) !important;
  box-shadow:
    0 12px 28px rgba(53, 18, 19, 0.08),
    0 0 0 4px rgba(255, 182, 171, 0.045),
    inset 0 1px 0 rgba(255, 255, 255, 0.16);
}

:deep(.el-form-item__error) {
  position: static !important;
  display: block !important;
  color: var(--error-soft) !important;
  font-size: 0.75rem !important;
  font-weight: 500 !important;
  margin-top: 8px !important;
  padding-left: 6px !important;
  letter-spacing: 0.015em !important;
  line-height: 1.4 !important;
  transform: none !important;
  text-align: left !important;
}

.submit-item {
  margin-top: 10px;
}

.gradient-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  height: 52px;
  background:
    radial-gradient(circle at 50% 0%, rgba(255, 255, 255, 0.11), transparent 62%),
    linear-gradient(135deg, rgba(241, 245, 255, 0.22) 0%, rgba(219, 228, 251, 0.15) 100%);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(244, 247, 255, 0.32);
  border-radius: var(--radius-control);
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.03em;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    0 16px 34px rgba(11, 20, 46, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    inset 0 -12px 20px rgba(255, 255, 255, 0.032);
}

.gradient-btn::before,
.gradient-btn::after {
  content: '';
  position: absolute;
  pointer-events: none;
}

.gradient-btn::before {
  inset: 1px;
  border-radius: calc(var(--radius-control) - 1px);
  border: 1px solid var(--surface-inner-stroke);
  mask-image: linear-gradient(to bottom, black, transparent 84%);
}

.gradient-btn::after {
  top: 0;
  left: 18%;
  width: 64%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.48), transparent);
  opacity: 0.62;
}

.gradient-btn:hover:not(:disabled) {
  background:
    radial-gradient(circle at 50% 0%, rgba(255, 255, 255, 0.12), transparent 62%),
    linear-gradient(135deg, rgba(244, 247, 255, 0.25) 0%, rgba(223, 231, 252, 0.17) 100%);
  border-color: rgba(247, 249, 255, 0.38);
  transform: translateY(-1px);
  box-shadow:
    0 20px 40px rgba(11, 20, 46, 0.17),
    inset 0 1px 0 rgba(255, 255, 255, 0.34);
}

.gradient-btn:active:not(:disabled) {
  transform: translateY(1px) scale(0.997);
  box-shadow:
    0 10px 18px rgba(11, 20, 46, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.24);
}

.gradient-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.btn-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.16), transparent);
  transition: left 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 0;
}

.gradient-btn:hover:not(:disabled) .btn-glow {
  left: 100%;
}

.btn-spinner {
  position: relative;
  z-index: 1;
  display: inline-block;
  width: 15px;
  height: 15px;
  margin-right: 10px;
  border-radius: 50%;
  border: 1.5px solid rgba(255, 255, 255, 0.18);
  border-top-color: rgba(255, 255, 255, 0.9);
  animation: spin 0.8s linear infinite;
  vertical-align: -2px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.btn-text {
  position: relative;
  z-index: 1;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-footer {
  margin-top: 36px;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 0.8125rem;
  position: relative;
  z-index: 1;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.form-footer p {
  margin: 0;
  color: rgba(203, 212, 236, 0.66);
}

@media (max-width: 1024px) {
  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
  }

  .glass-card {
    max-width: 420px;
    padding: 44px 36px;
  }
}

@media (max-width: 640px) {
  .login-wrapper {
    --radius-panel: 24px;
  }

  .login-right {
    padding: 20px;
  }

  .glass-card {
    padding: 36px 28px;
  }

  .form-header h2 {
    font-size: 1.75rem;
  }
}
</style>
