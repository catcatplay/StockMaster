<template>
  <div id="app">
    <!-- 登录页不显示布局 -->
    <template v-if="$route.path === '/login'">
      <router-view></router-view>
    </template>
    
    <!-- 主布局 -->
    <template v-else>
      <el-container class="app-wrapper">
        <!-- 侧边栏 -->
        <el-aside width="240px" class="app-sidebar">
          <div class="sidebar-logo">
            <el-icon :size="24" color="#fff" style="margin-right: 12px"><Box /></el-icon>
            <span v-if="!isCollapse">Stock Master</span>
          </div>
          
          <el-menu 
            :default-active="$route.path" 
            router
            background-color="#001529"
            text-color="rgba(255,255,255,0.65)"
            active-text-color="#fff"
            :collapse="isCollapse"
            class="sidebar-menu"
            :unique-opened="true">
            
            <!-- 设备类台账 -->
            <el-sub-menu index="device" v-if="hasDeviceMenu">
              <template #title>
                <el-icon><Box /></el-icon>
                <span>设备类台账</span>
              </template>
              <el-menu-item index="/device/goods" v-if="hasPermission('goods')">货物管理</el-menu-item>
              <el-menu-item index="/device/inbound" v-if="hasPermission('inbound')">入库记录</el-menu-item>
              <el-menu-item index="/device/outbound" v-if="hasPermission('outbound')">出库记录</el-menu-item>
            </el-sub-menu>
            
            <!-- 耗材类台账 -->
            <el-sub-menu index="consumable" v-if="hasConsumableMenu">
              <template #title>
                <el-icon><Sell /></el-icon>
                <span>耗材类台账</span>
              </template>
              <el-menu-item index="/consumable/goods" v-if="hasPermission('goods')">货物管理</el-menu-item>
              <el-menu-item index="/consumable/inbound" v-if="hasPermission('inbound')">入库记录</el-menu-item>
              <el-menu-item index="/consumable/outbound" v-if="hasPermission('outbound')">出库记录</el-menu-item>
            </el-sub-menu>
            
            <!-- 系统管理 -->
            <el-sub-menu index="system" v-if="hasSystemMenu">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统管理</span>
              </template>
              <el-menu-item index="/system/user" v-if="hasPermission('user')">用户管理</el-menu-item>
              <el-menu-item index="/system/role" v-if="hasPermission('role')">角色管理</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>

        <el-container class="main-container">
          <!-- 顶部导航 -->
          <el-header class="app-header">
            <div class="header-left">
              <!-- 面包屑或标题，暂时留空或放当前页面标题 -->
              <h3 class="page-title">{{ currentRouteTitle }}</h3>
            </div>
            
            <div class="header-right">
              <div class="user-info-area">
                <el-tag v-if="userInfo?.roleName" size="small" effect="plain" round class="role-tag">
                  {{ userInfo?.roleName }}
                </el-tag>
                
                <el-dropdown trigger="click" @command="handleCommand">
                  <span class="user-dropdown-link">
                    <el-avatar :size="32" class="user-avatar">
                      {{ (userInfo?.realName || userInfo?.username || 'U').charAt(0).toUpperCase() }}
                    </el-avatar>
                    <span class="username">{{ userInfo?.realName || userInfo?.username }}</span>
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                      <el-dropdown-item divided command="logout" style="color: #ef4444;">退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </el-header>
          
          <!-- 内容区域 -->
          <el-main class="app-content">
            <div class="content-shell">
              <router-view v-slot="{ Component }">
                <transition name="fade-transform" mode="out-in">
                  <component :is="Component" />
                </transition>
              </router-view>
            </div>
          </el-main>
        </el-container>
      </el-container>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box, Sell, Setting, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userInfo = ref(null)
const permissions = ref([])
const isCollapse = ref(false)

// 计算当前页面标题（简单映射，实际项目中可以从路由 meta 获取）
const currentRouteTitle = computed(() => {
  const pathMap = {
    '/device/goods': '设备 - 货物管理',
    '/device/inbound': '设备 - 入库记录',
    '/device/outbound': '设备 - 出库记录',
    '/consumable/goods': '耗材 - 货物管理',
    '/consumable/inbound': '耗材 - 入库记录',
    '/consumable/outbound': '耗材 - 出库记录',
    '/system/user': '用户管理',
    '/system/role': '角色管理'
  }
  return pathMap[route.path] || '库存管理系统'
})

const loadUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    const info = JSON.parse(userInfoStr)
    userInfo.value = info
    permissions.value = info.permissions || []
  }
}

const hasDeviceMenu = computed(() => {
  return hasPermission('goods') || hasPermission('inbound') || hasPermission('outbound')
})

const hasConsumableMenu = computed(() => {
  return hasPermission('goods') || hasPermission('inbound') || hasPermission('outbound')
})

const hasSystemMenu = computed(() => {
  return hasPermission('user') || hasPermission('role')
})

const hasPermission = (permission) => {
  return permissions.value.includes(permission)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    userInfo.value = null
    permissions.value = []
    ElMessage.success('退出成功')
    router.push('/login')
  }).catch(() => {})
}

watch(() => route.path, () => {
  if (route.path !== '/login') {
    loadUserInfo()
  }
})

onMounted(() => {
  loadUserInfo()
})
</script>

<style>
/* Reset & Layout */
#app {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.app-wrapper {
  height: 100%;
  position: relative;
  background:
    radial-gradient(circle at 22% 0%, rgba(129, 153, 236, 0.16), transparent 34%),
    radial-gradient(circle at 100% 18%, rgba(199, 214, 255, 0.15), transparent 26%),
    linear-gradient(180deg, #edf3ff 0%, #e7eefc 46%, #eaf1ff 100%);
}

.app-sidebar {
  position: relative;
  background: linear-gradient(180deg, rgba(18, 32, 68, 0.98) 0%, rgba(18, 32, 68, 0.96) 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow:
    18px 0 44px rgba(14, 23, 47, 0.12),
    inset -1px 0 0 rgba(255, 255, 255, 0.03);
  z-index: 10;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  overflow: hidden;
}

.app-sidebar::before,
.app-sidebar::after {
  content: '';
  position: absolute;
  pointer-events: none;
}

.app-sidebar::before {
  inset: 0;
  background:
    radial-gradient(circle at 20% 12%, rgba(121, 150, 255, 0.22), transparent 28%),
    radial-gradient(circle at 88% 88%, rgba(130, 146, 214, 0.18), transparent 30%);
  opacity: 0.9;
}

.app-sidebar::after {
  inset: 0;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.045), transparent 18%),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: auto, 44px 44px, 44px 44px;
  opacity: 0.16;
}

.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.04);
  color: white;
  font-size: 18px;
  font-weight: 600;
  overflow: hidden;
  white-space: nowrap;
  position: relative;
  z-index: 1;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: inset 0 -1px 0 rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(10px);
}

.sidebar-menu {
  border-right: none !important;
  flex: 1;
  position: relative;
  z-index: 1;
  padding: 18px 12px 20px;
  background: transparent !important;
}

.main-container {
  height: 100%;
  background: transparent;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.app-header {
  background: rgba(248, 251, 255, 0.78);
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  border-bottom: 1px solid rgba(198, 210, 233, 0.48);
  box-shadow:
    0 10px 28px rgba(22, 36, 72, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.55);
  z-index: 9;
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

.header-left .page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d2a44;
  margin: 0;
  letter-spacing: 0.01em;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info-area {
  display: flex;
  align-items: center;
  gap: 14px;
}

.role-tag {
  border: none;
  height: 28px;
  padding: 0 12px;
  background: rgba(92, 117, 202, 0.12);
  color: #4660a8;
  font-weight: 600;
  border-radius: 999px;
  box-shadow: inset 0 0 0 1px rgba(109, 132, 213, 0.16);
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #33425f;
  font-weight: 500;
  gap: 8px;
  padding: 6px 10px 6px 8px;
  border-radius: 14px;
  transition:
    background 0.2s ease,
    box-shadow 0.2s ease,
    color 0.2s ease;
}

.user-dropdown-link:hover {
  background: rgba(255, 255, 255, 0.62);
  box-shadow:
    0 10px 22px rgba(22, 36, 72, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  color: #1d2a44;
}

.user-avatar {
  margin-right: 2px;
  background: linear-gradient(135deg, #5e72d7 0%, #4f5dc4 100%);
  box-shadow:
    0 10px 20px rgba(53, 78, 157, 0.24),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.username {
  margin-right: 4px;
}

.app-content {
  padding: 24px 28px 28px;
  overflow-y: auto;
  position: relative;
  background:
    radial-gradient(circle at top right, rgba(205, 219, 255, 0.2), transparent 24%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.24), rgba(255, 255, 255, 0));
}

.content-shell {
  min-height: 100%;
}

/* 覆盖 Element Menu 样式以匹配深色主题 */
.sidebar-menu.el-menu,
.sidebar-menu .el-sub-menu__title,
.sidebar-menu .el-menu-item {
  background: transparent !important;
}

.sidebar-menu .el-sub-menu__title,
.sidebar-menu .el-menu-item {
  height: 46px;
  line-height: 46px;
  margin: 4px 0;
  border-radius: 14px;
  color: rgba(229, 235, 252, 0.74) !important;
  transition:
    color 0.2s ease,
    background-color 0.2s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.sidebar-menu .el-sub-menu__title .el-icon,
.sidebar-menu .el-menu-item .el-icon {
  color: rgba(214, 224, 252, 0.72);
  transition: color 0.2s ease;
}

.sidebar-menu .el-sub-menu__title:hover,
.sidebar-menu .el-menu-item:hover {
  color: #f7faff !important;
  background: rgba(255, 255, 255, 0.07) !important;
  transform: translateX(2px);
}

.sidebar-menu .el-sub-menu__title:hover .el-icon,
.sidebar-menu .el-menu-item:hover .el-icon {
  color: #f7faff;
}

.sidebar-menu .el-menu-item.is-active {
  color: #ffffff !important;
  background: linear-gradient(135deg, rgba(112, 136, 229, 0.36), rgba(85, 104, 187, 0.34)) !important;
  box-shadow:
    0 14px 28px rgba(10, 18, 39, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.22);
}

.sidebar-menu .el-menu-item.is-active .el-icon {
  color: #ffffff;
}

.sidebar-menu .el-sub-menu.is-active > .el-sub-menu__title {
  color: #f7faff !important;
}

.sidebar-menu .el-sub-menu .el-menu {
  background: transparent !important;
}

@media (max-width: 960px) {
  .app-header {
    padding: 0 20px;
  }

  .app-content {
    padding: 20px;
  }
}
</style>
