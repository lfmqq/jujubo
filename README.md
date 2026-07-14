# 桔桔波管理系统（Orange-Wave-Management-System）

一个基于 **Spring Boot 3 + Vue 3** 的前后端分离后台管理系统框架，开箱即用，包含完整的用户认证、权限控制（RBAC）、菜单管理、操作日志、数据统计与文件上传等后台管理常用功能，可作为新项目快速起步的通用骨架。

---

## 一、项目概览

```
system/
├── springboot3-admin/        # 后端：Spring Boot 3 后台服务
│   ├── src/main/java/        # Java 源码
│   ├── src/main/resources/   # 配置文件、MyBatis Mapper XML
│   ├── target/               # 构建产物（含可执行 jar）
│   ├── uploads/              # 文件上传目录（运行时生成）
│   ├── pom.xml               # Maven 构建文件
│   └── build.log
├── vue3-admin/               # 前端：Vue 3 管理后台
│   ├── src/                  # 源码（views / layout / stores / utils / router）
│   ├── public/
│   ├── dist/                 # 构建产物（生产打包）
│   ├── index.html
│   ├── vite.config.js        # Vite 配置（含代理）
│   └── package.json
├── uploads/                  # 根级上传目录
├── winget.err / winget.log   # 环境安装日志
└── README.md
```

| 模块 | 技术栈                       | 端口        | 路径前缀 |
| ---- | ---------------------------- | ----------- | -------- |
| 后端 | Spring Boot 3.1.12 / Java 17 | 8080        | `/api`   |
| 前端 | Vue 3.5 + Vite 8             | 5173（dev） | `/`      |

---

## 二、后端（springboot3-admin）

### 2.1 技术栈

| 类别     | 选型                                                         |
| -------- | ------------------------------------------------------------ |
| 基础框架 | Spring Boot 3.1.12（Java 17）                                |
| 持久层   | MyBatis-Plus 3.5.7（MySQL 8）                                |
| 安全框架 | Spring Security 6（无状态 / JWT）                            |
| 缓存     | Redis（spring-boot-starter-data-redis）                      |
| 工具     | Hutool 5.8.25、Lombok、Jackson（jsr310 时间支持）            |
| 鉴权令牌 | JJWT 0.11.5                                                  |
| 接口文档 | Knife4j OpenAPI3（4.5.0，访问 `/doc.html`）                  |
| 其他     | spring-boot-starter-validation（参数校验）、spring-boot-starter-aop（操作日志切面） |

### 2.2 核心配置

- `application.yml`：全局配置，激活环境 `dev`（部署改为 `prod`），服务端口 `8080`，`context-path=/api`，文件上传上限 10MB/20MB，JWT 密钥与过期时间（默认 24h）。
- `application-dev.yml`：本地开发，MySQL `localhost:3306/manager_system`，Redis `127.0.0.1:6379`，开启 SQL 控制台日志，上传目录 `./uploads/`。
- `application-prod.yml`：生产环境（服务器 `159.75.182.200`），通过 `forward-headers-strategy: native` 获取真实客户端 IP，关闭 SQL 日志。
- 切换环境：`java -jar xxx.jar --spring.profiles.active=prod`

### 2.3 包结构（`com.admin`）

```
com.admin
├── AdminApplication.java          # 启动类（@MapperScan("com.admin.mapper")）
├── controller/                    # 接口层
│   ├── AuthController             # 登录 / 登出
│   ├── CaptchaController          # 图形验证码
│   ├── UserController             # 用户管理 + 个人信息 + 改密 / 重置密码
│   ├── RoleController             # 角色管理
│   ├── DeptController             # 部门管理
│   ├── MenuController             # 菜单管理（树、用户菜单、启停）
│   ├── NotifyMessageController    # 通知管理
│   ├── OperLogController          # 操作日志（含前端日志上报接口）
│   ├── StatisticsController       # 数据概览 / 趋势 / 最新用户
│   ├── FileController             # 文件上传
│   └── HomeController             # 首页信息
├── entity/                        # 数据实体（对应 sys_* 表）
│   ├── SysUser / SysRole / SysDept / SysMenu
│   ├── SysUserRole / SysRoleMenu  # 关联表
│   ├── SysNotifyMessage / SysOperLog
├── mapper/                        # MyBatis-Plus Mapper 接口
├── service/ + service/impl/       # 业务层
├── config/                        # 配置类
│   ├── SecurityConfig             # 安全链、CORS、密码编码器
│   ├── JwtAuthenticationFilter    # JWT 拦截器
│   ├── RedisConfig / MyBatisPlusConfig / JacksonConfig
│   ├── Knife4jConfig              # 接口文档
│   ├── DataInitializer            # 启动时自动修复数据关联
│   └── MyMetaObjectHandler        # 自动填充 createTime/updateTime
├── filter/                        # JwtAuthenticationFilter
├── dto/                           # LoginDTO、FrontendOperLogDto
└── common/
    ├── annotation/Log             # 操作日志注解
    ├── enums/                     # BusinessType、OperatorType
    ├── security/                  # LoginUser、UserDetailsServiceImpl
    ├── result/                    # Result<T>、ResultCodeEnum（统一响应）
    ├── exception/                 # 全局异常处理器、ServiceException
    └── util/                      # JwtUtil、RedisUtil、IpUtil、CaptchaUtil、SecurityUtil
```

### 2.4 关键功能实现

- **认证流程**：登录（`/auth/login`）先校验 Redis 中的图形验证码（一次性），再经 Spring Security `AuthenticationManager` 校验账号密码（BCrypt 加密），通过后生成 JWT 并返回，同时把 `LoginUser` 缓存到 Redis（`login:user:{userId}`，与 JWT 同过期）。
- **无状态鉴权**：`SecurityConfig` 关闭 session（`STATELESS`），所有受保护接口需携带 `Authorization: Bearer <token>`；`JwtAuthenticationFilter` 解析 token 并重建 `Authentication`。
- **权限控制**：基于 RBAC，使用 `@PreAuthorize("hasAuthority('xxx')")` 进行方法级鉴权；菜单 `perms` 字段即权限标识；前端根据「用户菜单」动态生成路由与可显示的按钮。
- **操作日志**：通过 `@Log` 注解 + AOP（`LogAspect`）自动记录后端增删改查；前端页面访问与接口失败也会调用 `OperLogController` 的 frontend 上报接口，统一落入 `sys_oper_log`。
- **动态菜单 / 路由**：菜单表以树形结构维护（`type`：0 目录 / 1 菜单 / 2 按钮），前端 `utils/dynamicRouter.js` 将 `user-menu` 转为 Vue Router 路由。
- **数据自修复**：`DataInitializer` 在应用启动时检查并补全 admin 用户角色、通知/用户/操作日志的按钮权限与「系统监控」目录，确保功能开箱可用（仅修复、不覆盖已有数据）。

### 2.5 主要接口一览（`/api` 前缀）

| 模块     | 接口                                                         | 说明                                   |
| -------- | ------------------------------------------------------------ | -------------------------------------- |
| 认证     | `POST /auth/login`、`POST /auth/logout`、`GET /auth/captcha` | 登录、登出、验证码                     |
| 用户     | `/system/user/page`、`/system/user/{id}`、`POST /system/user`、`PUT /system/user`、`DELETE /system/user/{id}`、`/system/user/profile`、`/system/user/reset-password/{id}` | 分页、详情、增改删、个人信息、重置密码 |
| 角色     | `/system/role/...`                                           | 角色增删改查与权限分配                 |
| 部门     | `/system/dept/...`                                           | 部门树                                 |
| 菜单     | `/system/menu/tree`、`/system/menu/user-menu`、`POST/PUT/DELETE`、`/system/menu/toggle-status` | 菜单树、用户菜单、启停                 |
| 通知     | `/system/notify/...`                                         | 通知管理                               |
| 操作日志 | `/monitor/operlog/...`、`POST /monitor/operlog/frontend`     | 查询/删除/清空、前端日志上报           |
| 统计     | `/statistics/overview`、`/statistics/user-trend`、`/statistics/user-trend-week`、`/statistics/latest-users` | 概览、月/周趋势、最新用户              |
| 文件     | `POST /file/upload`                                          | 文件上传（返回可访问路径）             |

> 接口文档（Knife4j）：启动后访问 `http://localhost:8080/api/doc.html`

---

## 三、前端（vue3-admin）

### 3.1 技术栈

| 类别     | 选型                                        |
| -------- | ------------------------------------------- |
| 框架     | Vue 3.5（`<script setup>`）                 |
| 构建     | Vite 8                                      |
| UI       | Element Plus 2.14 + @element-plus/icons-vue |
| 状态管理 | Pinia 3（user / menu / tagsView / theme）   |
| 路由     | Vue Router 4（动态路由 + 权限守卫）         |
| 请求     | Axios（统一拦截、Token 注入、错误上报）     |
| 图表     | ECharts 6 + vue-echarts 8                   |
| 其他     | NProgress（路由进度条）、中文语言包         |

### 3.2 目录结构（`src`）

```
src/
├── main.js                 # 入口：挂载 Pinia、Router、ElementPlus、全局图标、主题初始化
├── App.vue
├── style.css               # 全局样式
├── router/index.js         # 静态路由 + 动态路由加载 + 路由守卫（鉴权、进度条、日志上报）
├── layout/                 # 整体布局
│   ├── index.vue           # Layout 根（含侧边栏 / 顶栏 / 标签栏 / 内容区）
│   └── components/
│       ├── Sidebar.vue / HorizontalSidebar.vue / TopMenu.vue / MenuItem.vue  # 菜单
│       ├── Navbar.vue      # 顶栏（用户、主题、退出）
│       ├── TagsView.vue    # 多标签标签页
│       ├── SettingDrawer.vue / SettingFloatButton.vue  # 主题设置
├── views/                  # 页面
│   ├── login/index.vue     # 登录（账号 + 验证码）
│   ├── home/               # 首页
│   ├── statistics/index.vue# 数据概览仪表盘（卡片 + ECharts 图表）
│   ├── monitor/operlog/    # 操作日志查看
│   ├── system/
│   │   ├── user/           # 用户管理
│   │   ├── role/           # 角色管理
│   │   ├── dept/           # 部门管理
│   │   ├── menu/           # 菜单管理
│   │   └── notify/         # 通知管理
│   └── redirect.vue        # 重定向 / 404 兜底
├── stores/                 # Pinia：user（Token/信息）、menu（动态菜单）、tagsView、theme
├── utils/
│   ├── request.js          # Axios 实例 + 拦截器（Token、401 跳登录、403 提示、失败日志上报）
│   ├── dynamicRouter.js    # 后端菜单 → 前端路由
│   ├── operlog.js          # 操作日志上报封装
│   ├── icons.js / date.js  # 图标映射、日期工具
└── styles/                 # variables.css / global.css
```

### 3.3 主要特性

- **动态路由 + 按钮级权限**：登录后拉取「用户菜单」，前端生成路由；页面内按钮依据 `v-hasPermi` 类权限标识显示/隐藏。
- **登录鉴权守卫**：未登录跳转 `/login?redirect=...`；刷新后自动重拉用户信息并重新注册动态路由；根路径 `/` 固定重定向到 `/home`。
- **多标签导航（TagsView）**：支持页签打开/关闭/刷新，刷新后状态保留。
- **主题切换**：明亮 / 暗黑主题，设置通过 `theme` store 持久化（挂载前初始化避免闪烁）。
- **操作日志联动**：页面访问与接口失败自动上报到后端 `sys_oper_log`，可在「系统监控 → 操作日志」查看。
- **数据可视化**：统计页使用 ECharts 展示用户注册月/周趋势、概览卡片与最新用户列表。

### 3.4 开发 / 构建

```bash
cd vue3-admin
npm install
npm run dev        # 开发服务器 http://localhost:5173，代理 /api → 后端 8080
npm run build      # 生产打包到 dist/
npm run preview    # 预览构建产物
```

`vite.config.js` 关键配置：开发端口 `5173`；`/api` 与 `/uploads` 代理到 `http://localhost:8080`（可用 `VITE_PROXY_TARGET` 覆盖）；`build` 阶段按依赖（echarts / element-plus / vue-vendor）分包，提升缓存命中率。

---

## 四、运行与部署

### 4.1 环境依赖

- JDK 17+
- Maven 3.8+
- MySQL 8（数据库名 `manager_system`）
- Redis 6+
- Node.js 18+（前端）

### 4.2 数据库初始化（重要）

> ⚠️ 当前仓库**未包含 SQL 建表脚本**，需自行根据 `com.admin.entity` 下的实体类（`SysUser`、`SysRole`、`SysDept`、`SysMenu`、`SysUserRole`、`SysRoleMenu`、`SysNotifyMessage`、`SysOperLog`）创建对应数据表（表名见各实体 `@TableName`）。表结构要点：
>
> - 表名统一为 `sys_*`；
> - 主键 `id` 为 `BIGINT AUTO_INCREMENT`；
> - `create_time` / `update_time` 由 `MyMetaObjectHandler` 自动填充；
> - `sys_menu` 含 `parent_id / menu_name / path / component / perms / type(0目录 1菜单 2按钮) / icon / sort / visible / always_show / status` 等字段。
>   应用启动后 `DataInitializer` 会自动补全 admin 角色、按钮权限与「系统监控」菜单，无需手工插入。

### 4.3 启动步骤

1. 启动 MySQL、Redis。

2. 后端：

   ```bash
   cd springboot3-admin
   mvn spring-boot:run        # 或 mvn package 后 java -jar target/springboot3-admin-1.0.0.jar
   ```

   接口地址：`http://localhost:8080/api`，文档：`http://localhost:8080/api/doc.html`

3. 前端：

   ```bash
   cd vue3-admin
   npm install && npm run dev
   ```

   浏览器打开 `http://localhost:5173`。

4. 生产部署：前端 `npm run build` 后将 `dist/` 由 Nginx 托管（反向代理 `/api` 到后端 8080），后端以 `prod` 环境运行。

---

## 五、默认账号说明

- 超级管理员：`id=1`，关联角色 `role_id=1`（超级管理员）。管理员可在「用户管理」中将用户密码重置为默认 `awei123456`。
- 登录需输入图形验证码（由 `/auth/captcha` 生成，存入 Redis，一次性使用）。

---

## 六、扩展建议

- 补全 SQL 建表脚本并纳入版本控制，便于一键初始化。
- 引入代码生成器（基于 `sys_menu` / 表元数据）提升 CRUD 开发效率。
- 增加「字典管理」「定时任务」「在线用户」等常见后台模块。
- 生产环境将 JWT 密钥、数据库连接等敏感信息移至环境变量或配置中心。
- 前端可按需接入权限指令（`v-hasPermi`）统一封装到全局，简化按钮权限判断。