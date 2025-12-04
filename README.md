# SmartHire 智能招聘平台

这是SmartHire智能招聘平台的完整项目，整合了前端、后端和管理员功能。

## 📁 项目结构

```
SmartHire/
├── SmartHire_Backend/          # Spring Boot 后端服务
│   ├── src/main/java/         # Java 源代码
│   ├── src/main/resources/    # 配置文件
│   ├── database/              # 数据库脚本
│   ├── docs/                  # 后端文档
│   └── pom.xml               # Maven 配置
├── SmartHire_Frontend/        # UniApp 前端项目
│   ├── src/                   # 前端源代码
│   │   ├── pages/            # 页面文件
│   │   ├── components/       # 组件文件
│   │   ├── services/         # API 服务
│   │   ├── store/            # 状态管理
│   │   └── static/           # 静态资源
│   ├── scripts/              # 构建脚本
│   ├── package.json          # 前端依赖配置
│   ├── vite.config.ts        # Vite 构建配置
│   └── tsconfig.json         # TypeScript 配置
├── docs/                     # 项目文档
├── LICENSE                   # 开源协议
└── README.md                # 项目说明
```

## 🛠 技术栈

### 后端 (Spring Boot)
- **框架**: Spring Boot 3.5.7 + Java 21
- **数据库**: MySQL 9.0 + MyBatis Plus
- **缓存**: Redis + Jedis
- **认证**: JWT + Spring Security
- **文档**: Swagger/OpenAPI 3
- **文件存储**: 阿里云 OSS
- **邮件服务**: Spring Boot Mail
- **构建工具**: Maven

### 前端 (UniApp)
- **框架**: UniApp (Vue 3 + TypeScript)
- **构建工具**: Vite 5.2.8
- **状态管理**: Pinia
- **样式**: SCSS
- **多端支持**: H5、微信小程序、支付宝小程序等

## 🚀 快速开始

### 1. 环境要求
- **Node.js**: 16.0+
- **Java**: 21
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Maven**: 3.6+
- **开发工具**: HBuilderX 或 VS Code

### 2. 后端启动

#### 数据库配置
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE smarthire CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库脚本
cd SmartHire_Backend/database
mysql -u root -p smarthire < init.sql
```

#### 启动后端服务
```bash
cd SmartHire_Backend
# 使用 Maven 启动
mvn spring-boot:run

# 或使用 IDEA 直接运行主类
# 主类路径: com.example.SmartHireBackendApplication
```

后端服务启动后访问：
- API接口: http://localhost:8080
- Swagger文档: http://localhost:8080/swagger-ui/index.html

### 3. 前端启动

#### 安装依赖
```bash
cd SmartHire_Frontend
# 安装 pnpm (推荐)
npm install -g pnpm

# 安装项目依赖
pnpm install
pnpm install u-charts
```

#### 启动开发服务器
```bash
# H5 端开发
pnpm dev:h5

# 微信小程序开发
pnpm dev:mp-weixin

# 其他平台请参考 package.json 中的 scripts
```

#### 构建生产版本
```bash
# H5 构建
pnpm build:h5

# 微信小程序构建
pnpm build:mp-weixin
```

## 💡 开发工具

### 前端开发
- **HBuilderX** (推荐): https://www.dcloud.io/hbuilderx.html
- **VS Code**: 配置uni-app插件
- **微信开发者工具**: 微信小程序开发

### 后端开发
- **IntelliJ IDEA**: Java开发
- **Postman**: API测试
- **MySQL Workbench**: 数据库管理

## 📱 功能模块

### HR端功能
- 🏠 **工作台**: 数据概览、快捷操作
- 👥 **岗位管理**: 发布、编辑、管理招聘职位
- 💬 **消息中心**: 与候选人沟通、面试安排
- 📊 **数据分析**: 招聘数据统计和分析
- 👤 **个人中心**: HR个人信息管理

### 管理员后台功能
- 🔐 **用户管理**: HR账号、候选人账号管理
- 🏢 **企业管理**: 企业信息审核和管理
- 📋 **岗位审核**: 招聘职位审核和管理
- 📈 **数据统计**: 平台运营数据分析
- ⚙️ **系统配置**: 平台参数配置

## 🤝 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📞 联系方式

如有问题或建议，请联系项目负责人或在项目群中讨论。

---

*本项目正在持续开发中，功能和文档会不断更新完善*
