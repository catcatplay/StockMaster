<template>
  <div id="app">
    <!-- 登录页不显示布局 -->
    <template v-if="$route.path === '/login'">
      <router-view></router-view>
    </template>
    
    <!-- 主布局 -->
    <template v-else>
      <el-container>
        <el-header style="background-color: #409EFF; color: white; display: flex; align-items: center; justify-content: space-between;">
          <h2>库存管家</h2>
          <div style="display: flex; align-items: center; gap: 15px;">
            <span>欢迎，{{ userInfo?.realName || userInfo?.username }}</span>
            <el-tag type="success">{{ userInfo?.roleName }}</el-tag>
            <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
          </div>
        </el-header>
        <el-container>
          <el-aside width="200px" style="background-color: #f5f5f5;">
            <el-menu :default-active="$route.path" router>
              <!-- 设备类台账 -->
              <el-sub-menu index="device" v-if="hasDeviceMenu">
                <template #title>
                  <el-icon><Box /></el-icon>
                  <span>设备类台账</span>
                </template>
                <el-menu-item index="/device/goods" v-if="hasPermission('goods')">
                  <span>货物管理</span>
                </el-menu-item>
                <el-menu-item index="/device/inbound" v-if="hasPermission('inbound')">
                  <span>入库记录</span>
                </el-menu-item>
                <el-menu-item index="/device/outbound" v-if="hasPermission('outbound')">
                  <span>出库记录</span>
                </el-menu-item>
              </el-sub-menu>
              
              <!-- 耗材类台账 -->
              <el-sub-menu index="consumable" v-if="hasConsumableMenu">
                <template #title>
                  <el-icon><Sell /></el-icon>
                  <span>耗材类台账</span>
                </template>
                <el-menu-item index="/consumable/goods" v-if="hasPermission('goods')">
                  <span>货物管理</span>
                </el-menu-item>
                <el-menu-item index="/consumable/inbound" v-if="hasPermission('inbound')">
                  <span>入库记录</span>
                </el-menu-item>
                <el-menu-item index="/consumable/outbound" v-if="hasPermission('outbound')">
                  <span>出库记录</span>
                </el-menu-item>
              </el-sub-menu>
              
              <!-- 系统管理 -->
              <el-sub-menu index="system" v-if="hasSystemMenu">
                <template #title>
                  <el-icon><Setting /></el-icon>
                  <span>系统管理</span>
                </template>
                <el-menu-item index="/system/user" v-if="hasPermission('user')">
                  <span>用户管理</span>
                </el-menu-item>
                <el-menu-item index="/system/role" v-if="hasPermission('role')">
                  <span>角色管理</span>
                </el-menu-item>
              </el-sub-menu>
            </el-menu>
          </el-aside>
          <el-main>
            <router-view></router-view>
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
import { Box, Sell, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userInfo = ref(null)
const permissions = ref([]) // 权限列表

// 初始化用户信息
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

// 根据后端返回的权限列表判断
const hasPermission = (permission) => {
  return permissions.value.includes(permission)
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

// 监听路由变化，每次路由变化时重新加载用户信息
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
#app {
  height: 100vh;
}

.el-container {
  height: 100%;
}

.el-header {
  padding: 0 20px;
}

.el-aside {
  border-right: 1px solid #e6e6e6;
}

.el-main {
  background-color: #fff;
  padding: 20px;
}

* {
  margin: 0;
  padding: 0;
}

body, html {
  height: 100%;
}
</style>
