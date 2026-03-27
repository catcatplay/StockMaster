[根目录](../../CLAUDE.md) > **backend**

# Backend - Spring Boot REST API

## 变更记录 (Changelog)

| 时间 | 操作 | 说明 |
|------|------|------|
| 2026-03-20T17:34:57 | 初始化 | 首次生成模块文档 |

## 模块职责

提供库存管理系统的全部后端 REST API，包括：用户认证（JWT）、角色权限管理、货物 CRUD、入库/出库记录管理、Excel 数据导出。

## 入口与启动

- **启动类**: `com.stockmaster.StockOutboundApplication`（`@SpringBootApplication` + `@MapperScan`）
- **端口**: `9555`
- **Profile**: `application.yml` -> 激活 `prod` 配置（可切换为 `dev`）

## 对外接口

### 认证 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录，返回 JWT Token + 用户信息 + 权限列表 |
| POST | `/api/auth/register` | 用户注册 |
| GET | `/api/auth/info` | 获取当前用户信息（需 Authorization 头） |

### 货物管理 `/goods`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/goods/add` | 添加货物 |
| PUT | `/goods/update` | 更新货物 |
| DELETE | `/goods/delete/{id}` | 删除货物（有入库/出库记录时禁止删除） |
| GET | `/goods/get/{id}` | 根据 ID 查询 |
| GET | `/goods/list` | 查询列表（支持 type/name/brand/model 筛选） |
| GET | `/goods/page` | 分页查询（出库员角色返回 GoodsDTO，不含单价） |
| GET | `/goods/search` | 按名称模糊搜索 |
| GET | `/goods/stock-count` | 获取指定类型货物总库存 |
| GET | `/goods/export` | 导出 Excel |

### 入库记录 `/inbound`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/inbound/create` | 创建入库记录（自动更新库存、填充货物信息） |
| GET | `/inbound/list` | 查询列表（支持 type 筛选） |
| GET | `/inbound/page` | 分页查询（支持 type/startTime/endTime） |
| GET | `/inbound/listByGoods/{goodsId}` | 按货物 ID 查询 |
| GET | `/inbound/listByTime` | 按时间范围查询 |
| GET | `/inbound/export` | 导出 Excel |
| PUT | `/inbound/updateSettlement` | 更新结算状态 |

### 出库记录 `/outbound`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/outbound/create` | 创建出库记录（校验库存、自动扣减） |
| GET | `/outbound/list` | 查询列表（支持 type 筛选） |
| GET | `/outbound/page` | 分页查询（支持 type/startTime/endTime） |
| GET | `/outbound/listByGoods/{goodsId}` | 按货物 ID 查询 |
| GET | `/outbound/listByTime` | 按时间范围查询 |
| GET | `/outbound/totalQuantity/{goodsId}` | 查询某货物累计出库数量 |
| GET | `/outbound/export` | 导出 Excel |

### 用户管理 `/api/user`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/list` | 分页查询用户（支持 username/roleId 筛选） |
| GET | `/api/user/{id}` | 按 ID 查询 |
| POST | `/api/user/add` | 添加用户 |
| PUT | `/api/user/update` | 更新用户 |
| DELETE | `/api/user/delete/{id}` | 删除用户 |
| PUT | `/api/user/status/{id}` | 更新用户状态 |

### 角色管理 `/api/role`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/role/list` | 分页查询角色 |
| GET | `/api/role/all` | 查询全部角色 |
| GET | `/api/role/{id}` | 按 ID 查询 |
| POST | `/api/role/add` | 添加角色 |
| PUT | `/api/role/update` | 更新角色 |
| DELETE | `/api/role/delete/{id}` | 删除角色 |

## 关键依赖与配置

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.3.12.RELEASE | Web 框架 |
| MyBatis Plus | 3.4.3 | ORM + 分页 |
| MySQL Connector | 8.0.28 | 数据库驱动 |
| Lombok | - | POJO 简化 |
| EasyExcel | 3.1.1 | Excel 导出 |
| Spring Security | - | 密码加密（BCrypt），CSRF 关闭，全部路径 permitAll |
| jjwt | 0.9.1 | JWT 生成与校验 |

### 配置文件

- `application.yml` - Profile 切换（默认 prod）
- `application-dev.yml` - 开发环境（root/root，SQL 日志输出）
- `application-prod.yml` - 生产环境（stock_master 账号，关闭 SQL 日志）

### 安全架构

1. `SecurityConfig` - 禁用 CSRF，无状态 Session，所有路径 permitAll（权限在拦截器层实现）
2. `JwtInterceptor` - 拦截所有请求（排除 `/api/auth/**`），校验 JWT Token，提取 userId/roleId 到 request attribute
3. `WebConfig` - 注册 JWT 拦截器
4. `CorsConfig` - 跨域配置（allowedOrigins = "*"）

## 数据模型

### Goods（货物）
`id`, `name`, `brand`, `model`, `batch`, `unitPrice(BigDecimal)`, `type(device/consumable)`, `remainingStock(映射stock_quantity)`, `totalStock`, `createTime`, `updateTime`

### InboundRecord（入库记录）
`id`, `goodsId`, `goodsName`, `brand`, `model`, `type`, `quantity`, `inboundTime`, `remark`, `inboundUser`, `settlementStatus`

### OutboundRecord（出库记录）
`id`, `goodsId`, `goodsName`, `brand`, `model`, `type`, `quantity`, `department`, `outboundTime`, `remark`, `outboundUser`

### User（用户）
`id`, `username`, `password(BCrypt)`, `realName`, `roleId`, `roleName`, `status(0禁用/1启用)`, `createTime`, `updateTime`

### Role（角色）
`id`, `roleName`, `roleCode`, `description`, `permissions(JSON)`, `createTime`, `updateTime`

## 核心业务逻辑

- **入库**: 创建入库记录 -> 自动填充货物信息 -> 保存记录 -> 增加库存（`remainingStock` + `totalStock`），事务保护
- **出库**: 创建出库记录 -> 校验库存充足 -> 保存记录 -> 扣减库存（仅 `remainingStock`），事务保护
- **库存更新**: 乐观锁（CAS）机制，最多重试 3 次
- **删除保护**: 有入库/出库记录的货物禁止删除
- **角色数据隔离**: 出库员查询货物时返回 `GoodsDTO`（不含 `unitPrice`）

## 测试与质量

- **测试目录**: `src/test/` - 目录不存在，无测试代码
- **全局异常处理**: `GlobalExceptionHandler` 捕获 `BusinessException` 和通用 `Exception`
- **自定义异常**: `BusinessException`（code + message）

## 常见问题 (FAQ)

1. **Q: 为什么 SecurityConfig 允许所有路径？**
   A: 权限校验由 `JwtInterceptor` 实现，Spring Security 仅用于提供 `BCryptPasswordEncoder`。

2. **Q: JWT 密钥在哪里？**
   A: 硬编码在 `JwtUtils.SECRET_KEY`（值为 `stockmaster_secret_key_2026`）。建议迁到配置文件。

3. **Q: Goods.remainingStock 对应哪个数据库字段？**
   A: 通过 `@TableField("stock_quantity")` 映射到 `stock_quantity` 列。

## 相关文件清单

```
backend/
  pom.xml
  src/main/java/com/stockmaster/
    StockOutboundApplication.java          -- 启动类
    common/Result.java                      -- 统一响应体
    config/
      CorsConfig.java                       -- 跨域配置
      ExcelStyleConfig.java                 -- Excel 样式
      JwtInterceptor.java                   -- JWT 拦截器
      JwtUtils.java                         -- JWT 工具
      MybatisPlusConfig.java                -- 分页插件
      SecurityConfig.java                   -- Spring Security
      WebConfig.java                        -- Web 拦截器注册
    controller/
      GoodsController.java                  -- 货物管理 API
      InboundRecordController.java          -- 入库管理 API
      OutboundRecordController.java         -- 出库管理 API
      LoginController.java                  -- 认证 API
      UserController.java                   -- 用户管理 API
      RoleController.java                   -- 角色管理 API
    dto/
      GoodsDTO.java                         -- 货物（不含单价）
      GoodsExportDTO.java                   -- 货物导出
      InboundRecordExportDTO.java           -- 入库导出
      OutboundRecordExportDTO.java          -- 出库导出
      LoginRequest.java                     -- 登录请求
      LoginResponse.java                    -- 登录响应
    entity/
      Goods.java, InboundRecord.java, OutboundRecord.java, User.java, Role.java
    exception/
      BusinessException.java                -- 业务异常
      GlobalExceptionHandler.java           -- 全局异常处理
    mapper/
      GoodsMapper.java, InboundRecordMapper.java, OutboundRecordMapper.java
      UserMapper.java, RoleMapper.java
    service/
      GoodsService.java, InboundRecordService.java, OutboundRecordService.java
      UserService.java, RoleService.java
    service/impl/
      GoodsServiceImpl.java, InboundRecordServiceImpl.java, OutboundRecordServiceImpl.java
      UserServiceImpl.java, RoleServiceImpl.java
    util/
      PermissionUtils.java                  -- 权限 JSON 解析
  src/main/resources/
    application.yml                         -- 主配置
    application-dev.yml                     -- 开发环境
    application-prod.yml                    -- 生产环境
```
