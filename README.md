# Steam 游戏商城

一个类似 Steam 的游戏展示与购买平台，集游戏展示、用户交互、商城功能于一体的综合性 Web 应用。

## 🛠 技术栈

- **Frontend**: Vue 3 + TypeScript + Vite + Element Plus + Pinia
- **Backend**: Spring Boot 3 + MyBatis + JWT
- **Database**: MySQL 8.0

## 🚀 启动指南 (How to Run)

1. 确保 Docker Desktop 已启动
2. 在项目根目录执行：
   ```bash
   docker compose up --build
   ```
3. 等待容器启动完成（首次构建需要几分钟）

## 🔗 服务地址 (Services)

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8000/api
- **Database**: localhost:3306

## 🧪 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 普通用户 | testuser | 123456 |
| 普通用户 | gamer001 | 123456 |

## 📁 项目结构

```
taskId728/
├── docker-compose.yml      # Docker 编排配置
├── frontend/               # Vue 3 前端项目
│   ├── src/
│   │   ├── api/           # API 请求封装
│   │   ├── components/    # 公共组件
│   │   ├── router/        # 路由配置
│   │   ├── store/         # Pinia 状态管理
│   │   ├── styles/        # 全局样式
│   │   ├── types/         # TypeScript 类型定义
│   │   └── views/         # 页面视图
│   ├── Dockerfile
│   └── nginx.conf
├── backend/                # Spring Boot 后端项目
│   ├── src/main/java/com/steam/
│   │   ├── config/        # 配置类
│   │   ├── controller/    # 控制器
│   │   ├── dto/           # 数据传输对象
│   │   ├── entity/        # 实体类
│   │   ├── mapper/        # MyBatis Mapper
│   │   ├── service/       # 服务层
│   │   └── util/          # 工具类
│   ├── Dockerfile
│   └── pom.xml
└── database/
    └── init.sql            # 数据库初始化脚本
```

## ✨ 功能特性

### 🎮 游戏展示模块
- 首页轮播图展示精选游戏
- 热销榜单、新品上架、特惠促销分区
- 游戏分类浏览（动作、角色扮演、射击等）
- 游戏搜索与筛选（价格、分类、折扣）
- 游戏详情页（截图、介绍、系统需求、评论）

### 👤 用户模块
- 用户注册/登录（JWT 认证）
- 个人中心（资料编辑、余额充值）
- 用户游戏库（已购买的游戏）
- 愿望单管理

### 🛒 商城模块
- 购物车功能（添加/移除/清空）
- 订单创建与支付
- 订单管理（查看、取消）
- 余额支付系统

### 💬 社区模块
- 游戏评分与评论
- 评论有帮助标记

## 🎨 UI 特色

- Steam 深色主题风格
- 响应式布局（支持PC/平板/手机）
- Element Plus 组件库
- 骨架屏加载状态
- 平滑过渡动画

## 📝 API 接口

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 游戏相关
- `GET /api/games/home` - 获取首页数据
- `GET /api/games/search` - 搜索游戏
- `GET /api/games/{id}` - 获取游戏详情
- `GET /api/categories` - 获取所有分类

### 用户相关
- `GET /api/user/profile` - 获取用户信息
- `PUT /api/user/profile` - 更新用户信息
- `POST /api/user/recharge` - 充值余额

### 购物车相关
- `GET /api/cart` - 获取购物车
- `POST /api/cart` - 添加到购物车
- `DELETE /api/cart/{gameId}` - 移除商品

### 订单相关
- `POST /api/orders` - 创建订单
- `POST /api/orders/{orderNo}/pay` - 支付订单
- `GET /api/orders` - 获取订单列表

## 📦 数据库说明

- `users` - 用户表
- `games` - 游戏表
- `categories` - 分类表
- `game_categories` - 游戏分类关联表
- `cart_items` - 购物车表
- `wishlist` - 愿望单表
- `orders` - 订单表
- `order_items` - 订单明细表
- `user_library` - 用户游戏库表
- `game_reviews` - 游戏评论表

## 🔐 安全特性

- JWT Token 认证
- 密码 BCrypt 加密存储
- 路由守卫保护
- API 请求拦截器
- 输入校验（前后端双重验证）

---

> 本项目仅供学习参考
