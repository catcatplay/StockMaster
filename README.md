# 📦 StockMaster - 库存管理系统

一个基于 Spring Boot + Vue 3 的现代化库存出入库管理系统，支持设备和耗材的分类管理，提供完善的权限控制和数据导出功能。

## ✨ 功能特性

### 核心功能
- 🏷️ **货物管理** - 设备和耗材分类管理，支持品牌、型号、批次等信息维护
- 📥 **入库管理** - 入库记录管理、结算状态跟踪、时间范围查询
- 📤 **出库管理** - 出库记录管理、使用部门记录、库存自动扣减
- 👥 **用户管理** - 用户账号管理、角色分配、状态控制
- 🔐 **角色权限** - 基于 RBAC 的动态权限控制，支持细粒度权限配置

### 技术亮点
- ✅ **动态权限控制** - 前后端联动的权限验证，支持菜单和数据级权限
- ✅ **角色数据隔离** - 出库员角色自动隐藏敏感信息（如单价）
- ✅ **分页查询** - 后端分页提升大数据量性能
- ✅ **Excel 导出** - 基于 EasyExcel 的样式化导出功能
- ✅ **JWT 认证** - 无状态的安全认证机制
- ✅ **类型分类** - 设备（device）和耗材（consumable）独立管理

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 2.3.12
- **ORM**: MyBatis Plus 3.4.3
- **数据库**: MySQL 8.0
- **安全**: Spring Security + JWT
- **导出**: EasyExcel 3.1.1
- **工具**: Lombok

### 前端
- **框架**: Vue 3.3.4
- **构建工具**: Vite 4.4.9
- **UI 组件**: Element Plus 2.3.14
- **路由**: Vue Router 4.2.4
- **HTTP 客户端**: Axios 1.5.0

## 📋 系统要求

- **JDK**: 1.8+
- **Maven**: 3.6+
- **Node.js**: 16+
- **MySQL**: 8.0+

## 🚀 快速开始

### 1. 数据库配置

创建数据库：
```sql
CREATE DATABASE stock_master DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改配置文件 `backend/src/main/resources/application-dev.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/stock_master?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 2. 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:9555` 启动

### 3. 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

### 4. 构建部署

#### 后端打包
```bash
cd backend
mvn clean package
java -jar target/stock-outbound-system-1.0.0.jar --spring.profiles.active=prod
```

#### 前端打包
```bash
cd frontend
npm run build
```

打包后的文件在 `frontend/dist` 目录，可部署到 Nginx 等 Web 服务器。

## 📁 项目结构

```
StockMaster/
├── backend/                    # 后端项目
│   ├── src/main/java/com/stockmaster/
│   │   ├── common/            # 通用类（统一返回结果）
│   │   ├── config/            # 配置类（JWT、Security、CORS）
│   │   ├── controller/        # 控制器层
│   │   ├── dto/               # 数据传输对象
│   │   ├── entity/            # 实体类
│   │   ├── mapper/            # MyBatis Mapper
│   │   └── service/           # 业务逻辑层
│   └── src/main/resources/
│       ├── application.yml           # 主配置文件
│       ├── application-dev.yml       # 开发环境配置
│       └── application-prod.yml      # 生产环境配置
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/               # API 接口封装
│   │   ├── router/            # 路由配置
│   │   ├── utils/             # 工具函数
│   │   ├── views/             # 页面组件
│   │   ├── App.vue            # 根组件
│   │   └── main.js            # 入口文件
│   └── vite.config.js         # Vite 配置
└── README.md                   # 项目文档
```

## 🔑 默认账号

系统初始化后，建议创建以下角色和用户：

| 角色     | 权限说明                           |
| -------- | ---------------------------------- |
| 管理员   | 全部功能权限                       |
| 入库员   | 货物管理、入库记录                 |
| 出库员   | 出库记录（货物管理仅查看，无单价） |

## 📝 核心功能说明

### 权限控制机制
- **动态菜单**: 根据用户角色动态显示菜单
- **路由守卫**: 前端路由级别权限验证
- **数据权限**: 出库员角色自动过滤敏感字段（如单价）
- **按钮权限**: 根据角色显示/隐藏操作按钮

### 货物分类管理
系统支持两种货物类型：
- **设备 (device)**: 固定资产类货物
- **耗材 (consumable)**: 消耗品类货物

每种类型独立管理，互不干扰。

### 数据导出功能
- 支持入库/出库记录导出为 Excel
- 可按时间范围筛选导出
- 自动应用样式格式化（表头加粗、列宽自适应）

## 🔧 开发指南

### API 接口规范
所有接口返回统一格式：
```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

### 权限标识说明
```javascript
permissions: ['goods', 'inbound', 'outbound', 'user', 'role']
```

### 添加新功能的步骤
1. 数据库添加表结构
2. 创建实体类（Entity）
3. 创建 Mapper 接口
4. 实现 Service 层
5. 实现 Controller 层
6. 前端创建 API 接口
7. 前端创建页面组件
8. 配置路由和权限

## ⚠️ 注意事项

1. **权限更新**: 修改用户权限后需要重新登录才能生效
2. **IDEA 依赖**: 新增 Maven 依赖后需刷新 Maven 项目或使用 `mvn clean install`
3. **数据同步**: 新增字段时需同步更新 Entity、InboundRecord、OutboundRecord 三个实体类
4. **HTTPS 部署**: 生产环境建议使用 HTTPS 协议
5. **跨域配置**: 已配置 CORS，如需修改请编辑 `CorsConfig.java`

## 🐛 常见问题

### Q: 登录后看不到菜单？
A: 检查角色是否分配了相应权限，权限变更后需重新登录。

### Q: Maven 依赖下载失败？
A: 检查 Maven 仓库配置，或使用 `mvn clean install -U` 强制更新。

### Q: 前端接口请求 404？
A: 检查后端服务是否启动，以及 `request.js` 中的 baseURL 配置。

### Q: Excel 导出乱码？
A: EasyExcel 已配置 UTF-8 编码，确保使用支持 UTF-8 的 Excel 软件打开。

## 📄 License

本项目仅供学习交流使用。

## 👨‍💻 开发者

如有问题或建议，欢迎提 Issue 或 Pull Request。

---

**最后更新时间**: 2026-01-22
