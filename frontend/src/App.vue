<template>
  <div id="app">
    <router-view v-if="isLoginPage" />

    <el-container v-else class="app-wrapper">
      <el-aside v-if="!isMobile" width="240px" class="app-sidebar">
        <div class="sidebar-logo">
          <el-icon :size="24" color="#fff"><Box /></el-icon>
          <span>Stock Master</span>
        </div>
        <el-menu
          :default-active="route.path"
          router
          background-color="#001529"
          text-color="rgba(255,255,255,0.65)"
          active-text-color="#fff"
          class="sidebar-menu"
          :unique-opened="true"
        >
          <el-sub-menu v-for="group in menuGroups" :key="group.index" :index="group.index">
            <template #title>
              <el-icon><component :is="group.icon" /></el-icon>
              <span>{{ group.title }}</span>
            </template>
            <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
              {{ item.label }}
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <el-container class="main-container">
        <el-header class="app-header" :class="{ 'mobile-header': isMobile }">
          <div class="header-left">
            <el-button
              v-if="isMobile"
              type="primary"
              class="mobile-menu-button"
              @click="drawerOpen = true"
            >
              <el-icon :size="22"><Menu /></el-icon>
            </el-button>
            <div>
              <p v-if="isMobile" class="mobile-brand-name">Stock Master</p>
              <h3 class="page-title">{{ currentRouteTitle }}</h3>
            </div>
          </div>

          <div class="header-right">
            <div class="user-info-area">
              <el-tag v-if="userInfo?.roleName && !isMobile" size="small" effect="plain" round class="role-tag">
                {{ userInfo.roleName }}
              </el-tag>

              <el-dropdown trigger="click" @command="handleCommand">
                <span class="user-dropdown-link">
                  <el-avatar :size="isMobile ? 30 : 32" class="user-avatar">
                    {{ userInitial }}
                  </el-avatar>
                  <span class="username">{{ displayName }}</span>
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

      <el-drawer
        v-model="drawerOpen"
        direction="ltr"
        size="280px"
        :with-header="false"
        class="mobile-drawer"
        append-to-body
      >
        <div class="drawer-sidebar">
          <div class="sidebar-logo drawer-logo">
            <el-icon :size="24" color="#fff"><Box /></el-icon>
            <span>Stock Master</span>
          </div>

          <div class="drawer-user-card">
            <el-avatar :size="44" class="user-avatar">
              {{ userInitial }}
            </el-avatar>
            <div class="drawer-user-meta">
              <strong>{{ displayName }}</strong>
              <span>{{ userInfo?.roleName || '系统用户' }}</span>
            </div>
          </div>

          <el-menu
            :default-active="route.path"
            router
            background-color="#001529"
            text-color="rgba(255,255,255,0.65)"
            active-text-color="#fff"
            class="sidebar-menu drawer-menu"
            :unique-opened="true"
            @select="handleMenuSelect"
          >
            <el-sub-menu v-for="group in menuGroups" :key="group.index" :index="group.index">
              <template #title>
                <el-icon><component :is="group.icon" /></el-icon>
                <span>{{ group.title }}</span>
              </template>
              <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
                {{ item.label }}
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </div>
      </el-drawer>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Box, Menu, Sell, Setting } from '@element-plus/icons-vue'
import { useViewport } from '@/composables/useViewport'

const router = useRouter()
const route = useRoute()
const userInfo = ref(null)
const permissions = ref([])
const drawerOpen = ref(false)
const { isMobile } = useViewport()

const isLoginPage = computed(() => route.path === '/login')

const displayName = computed(() => userInfo.value?.realName || userInfo.value?.username || '未登录用户')
const userInitial = computed(() => displayName.value.charAt(0).toUpperCase())

const currentRouteTitle = computed(() => {
  const pathMap = {
    '/device/goods': '设备 - 货物管理',
    '/device/inbound': '设备 - 入库记录',
    '/device/outbound': '设备 - 出库记录',
    '/device/outbound-cancel': '设备 - 出库取消记录',
    '/consumable/goods': '耗材 - 货物管理',
    '/consumable/inbound': '耗材 - 入库记录',
    '/consumable/outbound': '耗材 - 出库记录',
    '/consumable/outbound-cancel': '耗材 - 出库取消记录',
    '/system/user': '用户管理',
    '/system/role': '角色管理',
    '/profile': '个人信息'
  }

  return pathMap[route.path] || '库存管理系统'
})

const hasPermission = (permission) => permissions.value.includes(permission)

const menuGroups = computed(() => {
  const groups = []

  if (hasPermission('goods') || hasPermission('inbound') || hasPermission('outbound')) {
    groups.push({
      index: 'device',
      title: '设备类台账',
      icon: Box,
      items: [
        hasPermission('goods') ? { path: '/device/goods', label: '货物管理' } : null,
        hasPermission('inbound') ? { path: '/device/inbound', label: '入库记录' } : null,
        hasPermission('outbound') ? { path: '/device/outbound', label: '出库记录' } : null,
        hasPermission('outbound') ? { path: '/device/outbound-cancel', label: '出库取消记录' } : null
      ].filter(Boolean)
    })

    groups.push({
      index: 'consumable',
      title: '耗材类台账',
      icon: Sell,
      items: [
        hasPermission('goods') ? { path: '/consumable/goods', label: '货物管理' } : null,
        hasPermission('inbound') ? { path: '/consumable/inbound', label: '入库记录' } : null,
        hasPermission('outbound') ? { path: '/consumable/outbound', label: '出库记录' } : null,
        hasPermission('outbound') ? { path: '/consumable/outbound-cancel', label: '出库取消记录' } : null
      ].filter(Boolean)
    })
  }

  if (hasPermission('user') || hasPermission('role')) {
    groups.push({
      index: 'system',
      title: '系统管理',
      icon: Setting,
      items: [
        hasPermission('user') ? { path: '/system/user', label: '用户管理' } : null,
        hasPermission('role') ? { path: '/system/role', label: '角色管理' } : null
      ].filter(Boolean)
    })
  }

  return groups
})

const loadUserInfo = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) {
    userInfo.value = null
    permissions.value = []
    return
  }

  const info = JSON.parse(userInfoStr)
  userInfo.value = info
  permissions.value = info.permissions || []
}

const handleMenuSelect = () => {
  drawerOpen.value = false
}

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
    return
  }

  if (command === 'profile') {
    drawerOpen.value = false
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
    drawerOpen.value = false
    ElMessage.success('退出成功')
    router.push('/login')
  }).catch(() => {})
}

watch(() => route.fullPath, () => {
  if (route.path !== '/login') {
    loadUserInfo()
  }

  if (isMobile.value) {
    drawerOpen.value = false
  }
})

watch(isMobile, (mobile) => {
  if (!mobile) {
    drawerOpen.value = false
  }
})

onMounted(() => {
  loadUserInfo()
})
</script>

<style>
#app {
  min-height: var(--app-height, 100vh);
  width: 100%;
}

.app-wrapper {
  min-height: var(--app-height, 100vh);
  background:
    radial-gradient(circle at 22% 0%, rgba(129, 153, 236, 0.16), transparent 34%),
    radial-gradient(circle at 100% 18%, rgba(199, 214, 255, 0.15), transparent 26%),
    linear-gradient(180deg, #edf3ff 0%, #e7eefc 46%, #eaf1ff 100%);
}

.app-sidebar,
.drawer-sidebar {
  position: relative;
  background: linear-gradient(180deg, rgba(18, 32, 68, 0.98) 0%, rgba(18, 32, 68, 0.96) 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow:
    18px 0 44px rgba(14, 23, 47, 0.12),
    inset -1px 0 0 rgba(255, 255, 255, 0.03);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.app-sidebar::before,
.app-sidebar::after,
.drawer-sidebar::before,
.drawer-sidebar::after {
  content: '';
  position: absolute;
  pointer-events: none;
}

.app-sidebar::before,
.drawer-sidebar::before {
  inset: 0;
  background:
    radial-gradient(circle at 20% 12%, rgba(121, 150, 255, 0.22), transparent 28%),
    radial-gradient(circle at 88% 88%, rgba(130, 146, 214, 0.18), transparent 30%);
  opacity: 0.9;
}

.app-sidebar::after,
.drawer-sidebar::after {
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
  gap: 12px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.04);
  color: white;
  font-size: 18px;
  font-weight: 600;
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
  min-width: 0;
  min-height: var(--app-height, 100vh);
  display: flex;
  flex-direction: column;
}

.app-header {
  background: rgba(248, 251, 255, 0.78);
  min-height: 64px;
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

.mobile-header {
  padding: 10px 16px;
  min-height: 68px;
}

.header-left,
.header-right,
.user-info-area,
.user-dropdown-link {
  display: flex;
  align-items: center;
}

.header-left {
  gap: 12px;
  min-width: 0;
}

.mobile-menu-button {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  border: 1px solid rgba(112, 129, 214, 0.24);
  flex-shrink: 0;
  color: #4d5fc6;
  background: rgba(255, 255, 255, 0.62);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow:
    0 8px 18px rgba(46, 63, 126, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.mobile-menu-button:hover,
.mobile-menu-button:focus-visible {
  color: #4457be;
  border-color: rgba(94, 114, 215, 0.34);
  background: rgba(255, 255, 255, 0.82);
  box-shadow:
    0 10px 22px rgba(46, 63, 126, 0.14),
    inset 0 1px 0 rgba(255, 255, 255, 0.82);
}

.mobile-menu-button:active {
  color: #3f52b4;
  background: rgba(244, 247, 255, 0.92);
  box-shadow:
    0 4px 12px rgba(46, 63, 126, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.72);
}

.mobile-brand-name {
  margin: 0 0 4px;
  font-size: 12px;
  color: #7b89a5;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2a44;
  letter-spacing: 0.01em;
}

.user-info-area {
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
  cursor: pointer;
  color: #33425f;
  font-weight: 500;
  gap: 8px;
  padding: 6px 10px 6px 8px;
  border-radius: 14px;
  transition: background 0.2s ease, box-shadow 0.2s ease, color 0.2s ease;
}

.user-dropdown-link:hover {
  background: rgba(255, 255, 255, 0.62);
  box-shadow:
    0 10px 22px rgba(22, 36, 72, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  color: #1d2a44;
}

.user-avatar {
  background: linear-gradient(135deg, #5e72d7 0%, #4f5dc4 100%);
  box-shadow:
    0 10px 20px rgba(53, 78, 157, 0.24),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.username {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-content {
  flex: 1;
  padding: 24px 28px 28px;
  overflow: auto;
  background:
    radial-gradient(circle at top right, rgba(205, 219, 255, 0.2), transparent 24%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.24), rgba(255, 255, 255, 0));
}

.content-shell {
  min-height: 100%;
}

.drawer-user-card {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 18px 16px 0;
  padding: 14px;
  border-radius: 18px;
  color: #f7faff;
  background: rgba(255, 255, 255, 0.06);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.08);
}

.drawer-user-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.drawer-user-meta strong,
.drawer-user-meta span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.drawer-user-meta span {
  color: rgba(229, 235, 252, 0.72);
  font-size: 13px;
}

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

.sidebar-menu .el-menu-item.is-active .el-icon,
.sidebar-menu .el-sub-menu.is-active > .el-sub-menu__title {
  color: #ffffff;
}

.sidebar-menu .el-sub-menu .el-menu {
  background: transparent !important;
}

.mobile-drawer .el-drawer {
  background: transparent;
  box-shadow: none;
}

.mobile-drawer .el-drawer__body {
  padding: 0;
}

.drawer-sidebar {
  min-height: 100%;
}

@media (max-width: 960px) {
  .app-content {
    padding: 20px;
  }
}

@media (max-width: 767px) {
  .app-wrapper,
  .main-container {
    min-height: var(--app-height, 100vh);
  }

  .app-content {
    padding: 16px;
  }

  .username {
    max-width: 88px;
  }

  .page-title {
    font-size: 16px;
  }
}
</style>
