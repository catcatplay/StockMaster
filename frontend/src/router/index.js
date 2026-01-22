import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Login from '../views/Login.vue'
import GoodsManage from '../views/GoodsManage.vue'
import InboundRecord from '../views/InboundRecord.vue'
import OutboundRecord from '../views/OutboundRecord.vue'
import RoleManage from '../views/RoleManage.vue'
import UserManage from '../views/UserManage.vue'
import Profile from '../views/Profile.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/device/goods'
  },
  // 设备类台账
  {
    path: '/device/goods',
    name: 'DeviceGoods',
    component: GoodsManage,
    meta: { type: 'device', requiresAuth: true, permission: 'goods' }
  },
  {
    path: '/device/inbound',
    name: 'DeviceInbound',
    component: InboundRecord,
    meta: { type: 'device', requiresAuth: true, permission: 'inbound' }
  },
  {
    path: '/device/outbound',
    name: 'DeviceOutbound',
    component: OutboundRecord,
    meta: { type: 'device', requiresAuth: true, permission: 'outbound' }
  },
  // 耗材类台账
  {
    path: '/consumable/goods',
    name: 'ConsumableGoods',
    component: GoodsManage,
    meta: { type: 'consumable', requiresAuth: true, permission: 'goods' }
  },
  {
    path: '/consumable/inbound',
    name: 'ConsumableInbound',
    component: InboundRecord,
    meta: { type: 'consumable', requiresAuth: true, permission: 'inbound' }
  },
  {
    path: '/consumable/outbound',
    name: 'ConsumableOutbound',
    component: OutboundRecord,
    meta: { type: 'consumable', requiresAuth: true, permission: 'outbound' }
  },
  // 系统管理
  {
    path: '/system/role',
    name: 'RoleManage',
    component: RoleManage,
    meta: { requiresAuth: true, permission: 'role' }
  },
  {
    path: '/system/user',
    name: 'UserManage',
    component: UserManage,
    meta: { requiresAuth: true, permission: 'user' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  
  // 如果是登录页，直接放行
  if (to.path === '/login') {
    next()
    return
  }
  
  // 如果需要认证但未登录，跳转到登录页
  if (to.meta.requiresAuth && !token) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }
  
  // 检查权限
  if (to.meta.permission && userInfoStr) {
    const userInfo = JSON.parse(userInfoStr)
    const permissions = userInfo.permissions || []
    
    if (!permissions.includes(to.meta.permission)) {
      ElMessage.error('您没有权限访问该页面')
      next(from.path || '/device/goods')
      return
    }
  }
  
  next()
})

export default router
