[根目录](../../CLAUDE.md) > **frontend**

# Frontend - Vue 3 SPA

## 变更记录 (Changelog)

| 时间 | 操作 | 说明 |
|------|------|------|
| 2026-03-20T17:34:57 | 初始化 | 首次生成模块文档 |

## 模块职责

提供库存管理系统的 Web 用户界面，包含：用户登录、货物管理（设备类/耗材类）、入库记录、出库记录、用户管理、角色管理、个人信息等页面。

## 入口与启动

- **入口**: `src/main.js` -> 创建 Vue 应用，挂载 Element Plus（中文）+ Router
- **HTML**: `index.html`（`lang="zh-CN"`）
- **开发端口**: `3000`（Vite dev server）
- **路径别名**: `@` -> `./src`

## 对外接口（API 模块）

所有请求通过 `utils/request.js`（Axios 封装）发出：
- 自动附加 `Authorization: Bearer <token>` 头
- 响应拦截：`code !== 200` 报错，`401` 清除 token 并跳转登录页
- Blob 响应（文件下载）直接返回

### API 文件列表

| 文件 | 请求前缀 | 功能 |
|------|----------|------|
| `api/auth.js` | `/api/auth` | 登录、注册、获取用户信息 |
| `api/user.js` | `/api/user` | 用户 CRUD、状态更新 |
| `api/role.js` | `/api/role` | 角色 CRUD、获取全部角色 |
| `api/goods.js` | `/goods` | 货物 CRUD、搜索、库存统计、导出 |
| `api/inbound.js` | `/inbound` | 入库记录创建、查询、导出、结算状态更新 |
| `api/outbound.js` | `/outbound` | 出库记录创建、查询、导出、累计数量查询 |

**注意**: `auth.js`/`user.js`/`role.js` 使用 `/api` 前缀，而 `goods.js`/`inbound.js`/`outbound.js` 不使用。开发代理会将 `/api` 前缀 rewrite 去掉。

## 路由结构

| 路径 | 组件 | 说明 | 权限 |
|------|------|------|------|
| `/login` | `Login.vue` | 登录页 | 无需认证 |
| `/` | - | 重定向到 `/device/goods` | - |
| `/device/goods` | `GoodsManage.vue` | 设备货物管理 | goods |
| `/device/inbound` | `InboundRecord.vue` | 设备入库记录 | inbound |
| `/device/outbound` | `OutboundRecord.vue` | 设备出库记录 | outbound |
| `/consumable/goods` | `GoodsManage.vue` | 耗材货物管理 | goods |
| `/consumable/inbound` | `InboundRecord.vue` | 耗材入库记录 | inbound |
| `/consumable/outbound` | `OutboundRecord.vue` | 耗材出库记录 | outbound |
| `/system/user` | `UserManage.vue` | 用户管理 | user |
| `/system/role` | `RoleManage.vue` | 角色管理 | role |
| `/profile` | `Profile.vue` | 个人信息 | 仅需登录 |

### 路由守卫

- Token 检查：未登录用户访问需认证页面时重定向到 `/login`
- 权限检查：根据 `localStorage.userInfo.permissions` 与路由 `meta.permission` 匹配
- 设备类/耗材类通过 `meta.type` 区分，同一组件复用

## 关键依赖与配置

| 依赖 | 版本 | 用途 |
|------|------|------|
| Vue | ^3.3.4 | 核心框架 |
| Vue Router | ^4.2.4 | 路由 |
| Axios | ^1.5.0 | HTTP 客户端 |
| Element Plus | ^2.3.14 | UI 组件库 |
| Vite | ^4.4.9 | 构建工具 |
| @vitejs/plugin-vue | ^4.3.4 | Vite Vue 插件 |

### 构建配置 (vite.config.js)

- 输出目录: `dist/`
- Source map: 关闭
- 开发代理: `/api` -> `http://localhost:9555`（去掉 `/api` 前缀）

## 布局与导航 (App.vue)

- 侧边栏（240px）：深色主题，分三组菜单
  - 设备类台账（货物/入库/出库）
  - 耗材类台账（货物/入库/出库）
  - 系统管理（用户/角色）
- 顶部导航：页面标题 + 用户信息下拉（个人信息/退出登录）
- 菜单项根据用户权限动态显示/隐藏
- 登录页不显示侧边栏布局

## 可复用逻辑 (Composables)

### `useExport()`
封装 Excel 导出逻辑：调用 API 获取 blob -> 创建下载链接 -> 触发下载。

### `usePagination(loadData, options)`
封装分页逻辑：管理 `currentPage`/`pageSize`/`total`，提供 `handleSizeChange`/`handleCurrentChange`/`resetPage`。

## 工具函数

### `utils/date.js`
- `formatDate(date)` - 格式化为 `YYYY-MM-DD HH:mm:ss`
- `formatDateForApi(date)` - 同上，用于 API 请求参数

### `utils/request.js`
Axios 实例封装，baseURL 为 `/api`，超时 10 秒。

## 样式体系

- `styles/index.css` - 全局样式，使用 CSS 变量覆盖 Element Plus 默认主题
- 主色调: `#5668ca`（蓝紫色系）
- 毛玻璃效果（`backdrop-filter: blur`）
- 圆角设计（卡片 24px、按钮 14px、输入框 14px）
- 响应式适配（960px 断点）

## 测试与质量

- **测试**: 无测试代码，未配置测试框架
- **代码风格**: 无 ESLint / Prettier 配置

## 常见问题 (FAQ)

1. **Q: 设备类和耗材类为何共用同一组件？**
   A: 通过路由 `meta.type`（`device`/`consumable`）区分，组件内根据 type 请求不同类型数据。

2. **Q: 用户状态存储在哪里？**
   A: `localStorage`（`token` 和 `userInfo` JSON）。无 Vuex/Pinia 状态管理。

3. **Q: API 前缀不一致是什么原因？**
   A: 系统管理相关接口（auth/user/role）使用 `/api` 前缀，业务接口（goods/inbound/outbound）直接使用模块名。开发环境通过 Vite 代理统一转发。

## 相关文件清单

```
frontend/
  index.html                    -- HTML 入口
  package.json                  -- 依赖与脚本
  vite.config.js                -- Vite 配置
  src/
    main.js                     -- Vue 应用入口
    App.vue                     -- 根组件（布局 + 导航）
    router/index.js             -- 路由定义 + 守卫
    api/
      auth.js                   -- 认证 API
      user.js                   -- 用户 API
      role.js                   -- 角色 API
      goods.js                  -- 货物 API
      inbound.js                -- 入库 API
      outbound.js               -- 出库 API
    views/
      Login.vue                 -- 登录页
      GoodsManage.vue           -- 货物管理页
      InboundRecord.vue         -- 入库记录页
      OutboundRecord.vue        -- 出库记录页
      UserManage.vue            -- 用户管理页
      RoleManage.vue            -- 角色管理页
      Profile.vue               -- 个人信息页
    composables/
      useExport.js              -- 导出逻辑
      usePagination.js          -- 分页逻辑
    utils/
      request.js                -- Axios 封装
      date.js                   -- 日期格式化
    styles/
      index.css                 -- 全局样式
```
