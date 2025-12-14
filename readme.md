# JavaMall - 电商网站系统

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5-green.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-20.10-red.svg)](https://www.docker.com/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-pink.svg)](https://www.typescriptlang.org/)

## 项目简介

JavaMall是一个基于前后端分离架构的电商网站系统，提供完整的线上购物解决方案。该系统支持用户注册登录、商品浏览、购物车管理、订单处理以及管理员后台管理等核心功能，旨在为用户提供便捷的购物体验和高效的管理工具。

## 技术亮点

### 🎨 前端交互与视觉效果
- ✅ **呼吸效果动画**：登录和注册页面实现了背景呼吸效果，通过CSS动画实现淡入淡出和缩放效果
- ✅ **响应式设计**：适配不同屏幕尺寸，提供良好的移动端和桌面端体验
- ✅ **过渡动画**：页面元素切换和状态变化时的平滑过渡效果
- ✅ **渐变背景**：多处使用现代化的渐变背景，提升视觉吸引力
- ✅ **卡片式设计**：商品列表和管理界面采用卡片式布局，增强视觉层次感
- ✅ **悬停效果**：按钮和可交互元素的悬停动画效果

### ⚡ 性能优化
- ✅ **图片加载状态管理**：商品列表页实现了图片加载成功/失败的状态处理和备用图片机制
- ✅ **加载状态管理**：页面和组件加载时的状态提示，提升用户体验
- ✅ **表单验证**：前端表单验证，减少不必要的后端请求

### 🔒 安全与认证
- ✅ **JWT认证**：基于JSON Web Token的身份认证机制
- ✅ **密码加密**：用户密码的安全存储
- ✅ **请求拦截器**：自动为API请求添加JWT Token
- ✅ **响应拦截器**：处理Token过期等异常情况，自动登出
- ✅ **权限控制**：基于用户角色的权限管理（管理员/普通用户）

### 📊 数据可视化
- ✅ **销售统计图表**：使用Chart.js实现销售数据的可视化展示
- ✅ **月份销售热力图**：直观展示月度销售数据分布，支持颜色渐变

### 🧠 状态管理与数据处理
- ✅ **Pinia状态管理**：现代化的Vue状态管理库，支持组合式API
- ✅ **本地存储持久化**：用户认证信息和购物车数据的本地持久化
- ✅ **计算属性**：高效的数据计算和处理
- ✅ **类型安全**：使用TypeScript确保数据类型的一致性

### 🛠️ API服务与网络请求
- ✅ **统一API封装**：集中管理API请求，支持超时设置和错误处理
- ✅ **请求方法封装**：支持GET/POST/PUT/DELETE等HTTP方法
- ✅ **认证请求支持**：自动处理需要认证的API请求
- ✅ **查询参数处理**：自动将GET请求数据转换为查询参数
- ✅ **204 No Content处理**：特殊处理无内容响应

### 🗺️ 路由管理
- ✅ **基于角色的路由守卫**：根据用户角色（管理员/普通用户）控制路由访问
- ✅ **嵌套路由**：管理员功能采用嵌套路由结构，提升管理体验
- ✅ **动态路由**：支持根据权限动态生成路由
- ✅ **路由重定向**：登录后根据角色自动重定向到相应页面

### 📱 组件化设计
- ✅ **可复用组件**：如月份销售热力图组件
- ✅ **Props类型定义**：组件属性的类型安全
- ✅ **事件处理**：组件间的事件通信
- ✅ **样式封装**：使用scoped CSS确保样式隔离

### 🚀 开发与部署
- ✅ **Docker容器化**：前后端和数据库的完整Docker部署方案
- ✅ **Docker Compose编排**：一键启动整个应用栈
- ✅ **TypeScript支持**：提供更好的代码质量和开发体验
- ✅ **ESLint代码检查**：确保代码质量和一致性
- ✅ **Prettier代码格式化**：统一代码风格
- ✅ **Vite构建工具**：快速的开发和构建体验

### 📂 项目结构
- ✅ **模块化架构**：前后端均采用模块化设计，便于维护和扩展
- ✅ **清晰的目录结构**：代码组织清晰，便于定位和维护
- ✅ **类型定义**：集中的TypeScript类型定义

## 技术栈

### 前端技术栈
- **框架**: Vue.js 3.5
- **语言**: TypeScript
- **UI组件库**: Vuetify 3.11
- **路由**: Vue Router 4.6
- **状态管理**: Pinia 3.0
- **图表库**: Chart.js 4.5
- **HTTP客户端**: Axios 1.13
- **构建工具**: Vite 7.2
- **代码检查**: ESLint 9.39
- **代码格式化**: Prettier 3.6

### 后端技术栈
- **语言**: Java 21
- **Web框架**: Jakarta Servlet 6.0
- **数据库**: MySQL 8.0
- **JSON处理**: Gson 2.10
- **身份认证**: JWT 4.4
- **邮件服务**: Jakarta Mail 2.0

### 部署技术
- **容器化**: Docker
- **编排工具**: Docker Compose

## 功能列表

### 用户功能
- ✅ 用户注册与登录
- ✅ 商品浏览与详情查看
- ✅ 购物车管理（添加、删除、修改数量）
- ✅ 订单创建与支付
- ✅ 个人订单管理
- ✅ 个人信息管理

### 管理员功能
- ✅ 商品管理（添加、编辑、删除）
- ✅ 订单管理（查看、处理）
- ✅ 销售数据统计与多数据大屏展示
- ✅ 个人信息管理

## 快速开始

### 环境要求
- Docker 20.10+
- Docker Compose 3.8+

### 安装与运行

1. 克隆项目
```bash
git clone git@github.com:AlexBybye/Java_MAll.git
cd JavaMall
```

2. 使用Docker Compose启动项目
```bash
docker-compose up -d
```

3. 访问项目
- 前端访问地址: `http://localhost`
- 后端API地址: `http://localhost:8080`
- 数据库访问地址: `localhost:3306` (用户名: root, 密码: Mimashi1)

### 手动运行（可选）

#### 后端运行
1. 安装JDK 21
2. 导入数据库结构
```bash
mysql -u root -p < backend/SQL_Structrue/mall_system_*.sql
```
3. 编译并运行后端
```bash
cd backend
mvn clean package
mvn jetty:run
```

#### 前端运行
1. 安装Node.js 20.19+
2. 安装依赖
```bash
cd frontend
npm install
```
3. 运行开发服务器
```bash
npm run dev
```
4. 构建生产版本
```bash
npm run build
```

## 项目结构

### 前端结构
```plainText
frontend/src/
├── App.vue              # 应用入口组件
├── components/          # 通用组件
│   └── MonthHeatmap.vue # 月份销售热力图组件
├── main.ts              # 应用入口文件
├── router/              # 路由配置
│   └── index.ts
├── services/            # API服务
│   └── api.ts
├── store/               # 状态管理
│   └── authStore.ts
├── stores/              # 状态管理
│   ├── auth.ts          # 认证状态
│   ├── cart.ts          # 购物车状态
│   └── counter.ts       # 示例状态
├── types/               # TypeScript类型定义
│   └── index.ts
├── utils/               # 工具函数
│   ├── http.ts          # HTTP请求工具
│   └── imageUtils.ts    # 图片处理工具
└── views/               # 页面组件
    ├── AdminDashboardView.vue        # 管理员仪表盘
    ├── AdminOrderManagementView.vue  # 管理员订单管理
    ├── AdminProductManagementView.vue # 管理员商品管理
    ├── AdminStatsView.vue            # 管理员统计视图
    ├── CartView.vue                  # 购物车视图
    ├── ProductListView.vue           # 商品列表视图
    ├── ProductDetailView.vue         # 商品详情视图
    ├── LoginView.vue                 # 登录视图
    └── RegisterView.vue              # 注册视图
```
### 后端结构
```plainText
backend/src/main/java/com/mall/
├── dao/                 # 数据访问对象
│   ├── CartDAO.java
│   ├── CustomerDAO.java
│   ├── OrderDAO.java
│   ├── ProductDAO.java
│   └── StatsDAO.java
├── filter/              # 过滤器
│   └── AuthFilter.java  # 认证过滤器
├── model/               # 数据模型
│   ├── Cart.java
│   ├── Customer.java
│   ├── OrderItem.java
│   ├── OrderMaster.java
│   └── Product.java
├── servlet/             # Servlet控制器
│   ├── CartServlet.java
│   ├── CustomerServlet.java
│   ├── LoginServlet.java
│   ├── OrderServlet.java
│   ├── ProductServlet.java
│   ├── RegisterServlet.java
│   └── StatsServlet.java
└── util/                # 工具类
    ├── DBUtil.java      # 数据库工具
    ├── EmailUtil.java   # 邮件工具
    └── JWTUtil.java     # JWT工具
```
## 部署

### Docker Compose部署
项目提供了完整的Docker Compose配置文件，包含前端、后端和MySQL数据库。只需执行以下命令即可一键部署：

```bash
docker-compose up -d
```

### 环境变量配置

#### 后端环境变量
- `DB_URL`: 数据库连接URL
- `DB_USER`: 数据库用户名
- `DB_PASSWORD`: 数据库密码

#### MySQL环境变量
- `MYSQL_ROOT_PASSWORD`: MySQL根密码
- `MYSQL_DATABASE`: 初始数据库名称

## 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 许可证与联系方式

### 许可证
本项目采用MIT许可证，详情请查看LICENSE文件。

### 联系方式
- 项目维护者: [AlexBybye]
- 邮箱: [by9000p@foxmail.com]
- 项目地址: [https://github.com/AlexBybye/Java_MAll]

---

**感谢使用JavaMall！** 🛒