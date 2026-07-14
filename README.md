# 桔桔波管理系统（Jujube Wave Management System）

一套基于 **Spring Boot 3 + Vue 3** 的前后端分离后台管理系统脚手架，内置用户、角色、菜单、部门、消息通知等基础权限管理模块，开箱即用，可作为各类管理后台项目的起点。

---

## 一、技术栈

### 后端（springboot3-admin）

| 技术            | 版本               | 说明                    |
| --------------- | ------------------ | ----------------------- |
| Spring Boot     | 3.1.12             | 基础框架                |
| Spring Security | 3.1.12             | 安全框架 / 认证鉴权     |
| MyBatis-Plus    | 3.5.7              | ORM 持久层              |
| MySQL           | 8.x（Connector/J） | 关系型数据库            |
| Redis           | —                  | 缓存登录用户信息、Token |
| JWT（jjwt）     | 0.11.5             | 无状态 Token 认证       |
| Hutool          | 5.8.25             | 通用工具库              |
| Knife4j         | 4.5.0              | OpenAPI 3 接口文档      |
| Java            | 17                 | 运行环境                |

### 前端（vue3-admin）

| 技术         | 版本   | 说明                  |
| ------------ | ------ | --------------------- |
| Vue          | 3.5.x  | 前端框架              |
| Vite         | 8.x    | 构建工具 / 开发服务器 |
| Element Plus | 2.14.x | UI 组件库             |
| Vue Router   | 4.x    | 路由                  |
| Pinia        | 3.x    | 状态管理              |
| Axios        | 1.18.x | HTTP 请求             |
| NProgress    | 0.2.0  | 路由加载进度条        |

---

## 二、目录结构

```
system/
├── springboot3-admin/        # 后端服务
│   ├── pom.xml               # Maven 配置
│   ├── src/main/java/com/admin/
│   │   ├── controller/       # 接口层
│   │   ├── service/          # 业务层（含 impl/）
│   │   ├── mapper/           # MyBatis-Plus Mapper
│   │   ├── entity/           # 数据库实体
│   │   ├── config/           # 配置类（Security/Redis/MyBatis/JWT 等）
│   │   ├── filter/           # JWT 认证过滤器
│   │   ├── common/           # 统一返回、异常、工具、安全
│   │   └── dto/              # 数据传输对象
│   └── src/main/resources/
│       ├── application.yml   # 主配置
│       └── mapper/           # MyBatis XML
├── vue3-admin/               # 前端项目
│   ├── src/
│   │   ├── views/            # 页面（login/dashboard/system/*）
│   │   ├── layout/           # 整体布局
│   │   ├── router/           # 路由与守卫
│   │   ├── stores/           # Pinia 状态（user/menu/tagsView/theme）
│   │   ├── utils/            # 请求封装、日期、图标
│   │   └── components/       # 公共组件
│   ├── package.json
│   └── vite.config.js        # 含 /api、/uploads 代理
└── uploads/                  # 文件上传目录（运行时生成）