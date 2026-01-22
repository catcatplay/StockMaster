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
                    <el-avatar :size="32" style="background: #4f46e5; margin-right: 8px">
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
            <router-view v-slot="{ Component }">
              <transition name="fade-transform" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
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
}

.app-sidebar {
  background-color: #001529;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  z-index: 10;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
}

.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #002140;
  color: white;
  font-size: 18px;
  font-weight: 600;
  overflow: hidden;
  white-space: nowrap;
}

.sidebar-menu {
  border-right: none !important;
  flex: 1;
}

.main-container {
  height: 100%;
  background-color: var(--bg-color-page);
  display: flex;
  flex-direction: column;
}

.app-header {
  background: white;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 9;
}

.header-left .page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.role-tag {
  border: none;
  background: rgba(79, 70, 229, 0.1);
  color: #4f46e5;
  font-weight: 600;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #374151;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.user-dropdown-link:hover {
  background: #f3f4f6;
}

.username {
  margin-right: 4px;
}

.app-content {
  padding: 24px;
  overflow-y: auto;
  position: relative;
}

/* 覆盖 Element Menu 样式以匹配深色主题 */
.el-menu-item.is-active {
  background-color: var(--el-color-primary) !important;
  color: white !important;
}

.el-menu-item:hover {
  color: white !important;
}

.el-sub-menu__title:hover {
  color: white !important;
}
</style>
