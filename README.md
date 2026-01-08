# SmartHire 智能招聘软件

**同济大学计算机科学与技术学院**

**软件工程课程设计项目**

## 项目成员

尹诚成，刘震，于闻达，徐浩然

## 项目概述

SmartHire 是一个基于向量语义匹配、LLM智能分析的智能招聘平台，采用 Spring Boot、FastAPI 和 UniApp 技术栈构建，为求职者、HR 和管理员提供完整的招聘解决方案。平台集成了 AI 能力，支持智能匹配、职业规划和面试准备等功能。

## 项目结构

```
SmartHire/
├── SmartHire_Backend/          # Spring Boot 后端服务
├── SmartHire_AI/               # Python AI 服务 (FastAPI)
├── SmartHire_Client/           # UniApp 求职者端
├── frontend-web/               # Vue 3 管理员后台
├── database/                   # 数据库脚本
└── docs/                       # 项目文档
```

## 技术栈

### 后端服务 (Java)

- Spring Boot 3.5.7 + Java 21
- MySQL 9.0 + MyBatis Plus
- Redis 6.0+ + Jedis
- JWT + Spring Security
- RabbitMQ
- WebSocket
- Swagger/OpenAPI 3

### AI 服务 (Python)
- FastAPI
- LangChain4j (通义千问)
- Milvus (向量数据库)
- Sentence Transformers

### 前端应用
- **求职者端**: UniApp (Vue 3 + TypeScript) + Vite
- **管理员后台**: Vue 3 + TypeScript + Naive UI

## 环境要求

- Node.js: 16.0+
- Java: 21
- Python: 3.8+
- MySQL: 8.0+ (推荐 9.0)
- Redis: 6.0+
- Maven: 3.6+
- RabbitMQ: 3.8+ (可选)

## 配置与启动

### 1. 数据库配置

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE smarthire CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库脚本
cd database
mysql -u root -p smarthire < create_table.sql
```

### 2. Java 后端服务配置

#### 配置文件
修改 `SmartHire_Backend/src/main/resources/application.yml` 中的以下配置项：
- 数据库连接信息
- Redis 连接信息
- JWT 密钥配置
- 阿里云 OSS 配置
- RabbitMQ 配置（如使用）

#### 启动方式

```bash
cd SmartHire_Backend

# 方式1: Maven 启动
mvn spring-boot:run

# 方式2: 打包后运行
mvn clean package
java -jar target/SmartHire_Backend-0.0.1-SNAPSHOT.jar
```

### 3. Python AI 服务配置

#### 环境准备

```bash
cd SmartHire_AI

# 创建虚拟环境（推荐）
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 安装依赖
pip install -r requirements.txt
```

#### 配置文件
修改 `SmartHire_AI/config.yaml` 中的以下配置项：
- `database`: MySQL 数据库连接信息
- `milvus`: 向量数据库配置
- `jwt`: JWT 密钥配置
- `qwen`: 通义千问 API 配置

#### 启动方式

```bash
cd SmartHire_AI

# 开发模式启动
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# 生产模式启动
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

### 4. Vue-UniApp 前端配置

#### 求职者端 (UniApp)

```bash
cd SmartHire_Client

# 安装依赖（推荐使用 pnpm）
npm install -g pnpm
pnpm install

# 配置 API 地址
# 修改 src/services/config.json 中的后端服务地址

# 启动开发服务器
pnpm dev:h5              # H5 端
pnpm dev:mp-weixin       # 微信小程序

# 构建生产版本
pnpm build:h5
pnpm build:mp-weixin
```

#### 管理员后台 (Vue 3)

```bash
cd frontend-web

# 安装依赖
npm install
# 或使用 pnpm
pnpm install

# 配置 API 地址
# 修改 src/utils/request.ts 中的 baseURL

# 启动开发服务器
npm run dev
# 或
pnpm dev

# 构建生产版本
npm run build
# 或
pnpm build
```

## 项目文档

- [后端开发文档](./docs/backend/)
  - [后端目录结构文档](./docs/backend/后端目录结构文档.md)
  - [后端命名规范文档](./docs/backend/后端命名规范文档.md)
  - [后端开发说明事项](./docs/backend/后端开发说明事项.md)
- [JWT认证方案技术文档](./docs/JWT认证方案技术文档.md)
- [WebSocket前端使用指南](./docs/websocket_frontend_guide.md)
- [Git提交规范](./docs/git提交规范.md)
- [数据库架构说明](./DATABASE_SCHEMA.md)

## 功能模块

### 求职者端
- 职位搜索与筛选
- 简历管理与投递
- 投递记录查询
- 消息中心
- AI 职业规划
- 面试准备建议

### HR 端
- 岗位发布与管理
- 简历筛选与评估
- 候选人匹配推荐
- 消息沟通
- 数据分析

### 管理员后台
- 用户管理
- 企业审核
- 岗位审核
- 投诉处理
- 数据统计

## 部署说明

### Docker 部署

后端和 AI 服务均提供了 Docker 部署配置：

- **后端服务**: `SmartHire_Backend/deploy/`
  - `Dockerfile`: 镜像构建文件
  - `docker-compose.yml`: 容器编排配置
  - `deploy.sh`: 部署脚本

- **AI 服务**: `SmartHire_AI/`
  - `Dockerfile`: 镜像构建文件
  - `docker-compose.yml`: 容器编排配置

### Nginx 配置

项目根目录提供了 Nginx 配置文件 `smarthire_nginx_fixed.conf`，可用于生产环境部署。

## 开发工具推荐

- **Java 开发**: IntelliJ IDEA
- **Python 开发**: PyCharm / VS Code
- **前端开发**: VS Code / HBuilderX
- **API 测试**: Postman / Apifox
- **数据库管理**: MySQL Workbench / Navicat
- **Redis 管理**: Redis Desktop Manager

## License

详见 [LICENSE](./LICENSE) 文件
