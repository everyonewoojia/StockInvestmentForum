# 股票基金投资论坛

一个前后端分离的股票基金投资论坛 Web 项目，面向投资交流、板块讨论、内容审核、用户认证、关注互动和运营管理等场景。

项目当前拆分为独立目录：

```text
stock-forum-standalone
├─ frontend        # Vue 3 + uni-app H5 前端
├─ backend         # Spring Boot 后端
├─ README.md
└─ .gitignore
```

## 功能概览

已实现的核心功能：

- 用户注册、登录、JWT 鉴权
- 用户资料、投资偏好、基础认证、实名/人脸认证申请、专业认证申请、风险评估
- 动态板块：A 股、港股、美股、基金、价值投资、量化投资、新股新债、宏观策略、公司研究、问答求助
- 发帖、长文、短讨论、投票类型入口
- 内容审核流：发帖后进入待审核，管理员审核通过后展示
- 帖子列表、帖子详情页、浏览数、点赞数、收藏数、评论数
- 多级评论基础结构
- 搜索：关键词、帖子、用户、股票/基金代码联想
- 关注用户、关注动态
- 群组创建、加入群组
- 私信发送
- 举报内容、后台处理举报
- 管理后台：运营仪表盘、内容审核、举报处理、板块管理、用户管理、敏感词管理
- Flyway 自动建表和初始化种子数据

当前预留但未真实打通的外部服务：

- 阿里云短信
- SMTP 邮件
- 微信开放平台登录
- 微博 OAuth 登录
- 腾讯云实名/人脸核身
- OSS 对象存储
- Redis
- Elasticsearch/OpenSearch

这些配置项已经预留在后端配置文件中，后续拿到真实服务商账号和密钥后可以继续接入。

## 技术栈

前端：

- Vue 3
- uni-app H5
- Vite

后端：

- Java 8
- Spring Boot 2.7.18
- Spring MVC
- Spring Data JPA
- JdbcTemplate
- Flyway
- MySQL
- H2 测试数据库
- JWT 鉴权

数据库迁移：

- Flyway migration 位于：

```text
backend/src/main/resources/db/migration
```

主要迁移文件：

```text
V1__init.sql       # 原始基础表
V2__forum.sql      # 股票基金论坛表结构和种子数据
```

## 目录说明

```text
frontend/
├─ src/
│  ├─ pages/
│  │  ├─ Index.vue       # 论坛主页面、Feed、帖子详情、管理后台等
│  │  └─ Login.vue       # 登录注册页
│  ├─ utils/
│  │  ├─ request.js      # 请求封装，默认 API 根路径 /api/forum
│  │  └─ auth.js         # 登录信息本地存储
│  ├─ App.vue
│  └─ pages.json
├─ static/
├─ package.json
├─ package-lock.json
├─ vite.config.js        # H5 代理配置
└─ index.html
```

```text
backend/
├─ src/main/java/com/stock/forum/
│  ├─ forum/
│  │  ├─ ForumController.java
│  │  ├─ ForumService.java
│  │  ├─ ForumDataAccess.java
│  │  └─ ForumDtos.java
│  ├─ auth/
│  ├─ common/
│  ├─ config/
│  └─ ...
├─ src/main/resources/
│  ├─ application.yml
│  ├─ application-test.yml
│  └─ db/migration/
├─ src/test/java/
├─ pom.xml
└─ README.md
```

## 环境要求

建议环境：

- JDK 8
- Maven 3.6+
- Node.js 18+
- MySQL 8.x
- DataGrip 或其他 MySQL 客户端

检查命令：

```powershell
java -version
mvn -version
node -v
npm -v
```

## 数据库准备

如果只想快速本地测试，可以使用 H2 测试配置，不需要手动建 MySQL 数据库。

如果要连接 MySQL，请先创建数据库。假设你的数据库名叫 `standalone`：

```sql
CREATE DATABASE standalone
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

后端启动成功后，Flyway 会自动创建论坛表，不需要手动建表。

常见论坛表包括：

```text
forum_users
forum_boards
forum_posts
forum_comments
forum_interactions
forum_follows
forum_reports
forum_sensitive_words
forum_verifications
forum_risk_assessments
forum_groups
forum_group_members
forum_messages
forum_notifications
forum_audit_logs
forum_stock_symbols
flyway_schema_history
```

在 DataGrip 中确认：

```sql
SELECT DATABASE();
SHOW TABLES;
SELECT * FROM flyway_schema_history;
```

## 后端配置

后端配置文件：

```text
backend/src/main/resources/application.yml
backend/src/main/resources/application-test.yml
```

### 本地 H2 测试配置

使用 `test` profile 时，后端使用 H2 内存数据库：

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

这种方式不会在 MySQL 中创建表。

### MySQL 配置

在 PowerShell 当前窗口中配置环境变量：

```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3306/standalone?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="你的MySQL密码"
$env:JWT_SECRET="replace-with-a-long-random-secret-for-forum"
$env:JWT_EXPIRE_MINUTES="10080"
```

然后启动：

```powershell
cd backend
mvn spring-boot:run
```

注意：环境变量只对当前 PowerShell 窗口有效。如果换了新窗口，需要重新设置。

### Maven 本地仓库问题

如果启动时报：

```text
org/apache/commons/io/FilenameUtils
```

说明全局 Maven 缓存里有损坏依赖。可以使用项目内 Maven 仓库：

```powershell
mvn "-Dmaven.repo.local=D:\medicine-assistant\medicine-assistant\stock-forum-standalone\backend\.m2\repository" spring-boot:run
```

测试也可以这样跑：

```powershell
mvn "-Dmaven.repo.local=D:\medicine-assistant\medicine-assistant\stock-forum-standalone\backend\.m2\repository" test
```

## 前端配置

前端请求根路径在：

```text
frontend/src/utils/request.js
```

默认：

```js
BASE_URL = '/api/forum'
```

H5 开发代理在：

```text
frontend/vite.config.js
```

默认代理：

```js
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

如果后端端口改成 `8081`，需要同步修改：

```js
target: 'http://localhost:8081'
```

## 启动项目

### 1. 启动后端

MySQL 模式：

```powershell
cd D:\medicine-assistant\medicine-assistant\stock-forum-standalone\backend

$env:MYSQL_URL="jdbc:mysql://localhost:3306/standalone?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="你的MySQL密码"
$env:JWT_SECRET="replace-with-a-long-random-secret-for-forum"

mvn "-Dmaven.repo.local=D:\medicine-assistant\medicine-assistant\stock-forum-standalone\backend\.m2\repository" spring-boot:run
```

H2 演示模式：

```powershell
cd 文件路径
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

后端默认地址：

```text
http://localhost:8080
```

API 根路径：

```text
http://localhost:8080/api/forum
```

### 2. 启动前端

第一次运行：

```powershell
cd 文件路径
npm.cmd install
npm.cmd run dev:h5
```

前端地址：

```text
http://localhost:5173/
```

## 演示账号

后端启动后会自动初始化演示用户。

```text
管理员：admin / forum-admin-2026
专业用户：analyst / analyst123
普通用户：investor / investor123
```

如果使用 H2，每次重启后数据会重新初始化。

如果使用 MySQL，数据会持久化保存。

## 后端接口概览

统一响应格式：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

主要接口：

```text
POST   /api/forum/auth/register
POST   /api/forum/auth/login
GET    /api/forum/auth/me

PUT    /api/forum/users/me
POST   /api/forum/users/me/verifications
POST   /api/forum/users/me/risk-assessment

GET    /api/forum/boards
GET    /api/forum/posts
POST   /api/forum/posts
GET    /api/forum/posts/{postId}
GET    /api/forum/posts/{postId}/comments
POST   /api/forum/posts/{postId}/comments
POST   /api/forum/posts/{postId}/interactions

POST   /api/forum/reports
POST   /api/forum/social/follow/{targetUserId}
DELETE /api/forum/social/follow/{targetUserId}
GET    /api/forum/social/following-feed

GET    /api/forum/search
GET    /api/forum/search/suggest

GET    /api/forum/groups
POST   /api/forum/groups
POST   /api/forum/groups/{groupId}/join

POST   /api/forum/messages
GET    /api/forum/messages/{peerId}

GET    /api/forum/admin/dashboard
GET    /api/forum/admin/review/posts
POST   /api/forum/admin/review/posts/{postId}
GET    /api/forum/admin/reports
POST   /api/forum/admin/reports/{reportId}/resolve
GET    /api/forum/admin/users
POST   /api/forum/admin/users/{userId}/violation
POST   /api/forum/admin/boards
PUT    /api/forum/admin/boards/{boardId}
DELETE /api/forum/admin/boards/{boardId}
GET    /api/forum/admin/sensitive-words
POST   /api/forum/admin/sensitive-words
DELETE /api/forum/admin/sensitive-words/{wordId}
```

需要登录的接口要带请求头：

```text
Authorization: Bearer <token>
```

## 测试

后端测试：

```powershell
cd backend
mvn test
```

如果遇到 Maven 缓存问题：

```powershell
mvn "-Dmaven.repo.local=D:\medicine-assistant\medicine-assistant\stock-forum-standalone\backend\.m2\repository" test
```

前端构建：

```powershell
cd frontend
npm.cmd run build:h5
```

## 第三方服务配置

以下配置当前为预留项。没有真实密钥时，不影响核心论坛功能运行。

短信：

```powershell
$env:FORUM_SMS_PROVIDER="aliyun"
$env:ALIYUN_SMS_ACCESS_KEY_ID=""
$env:ALIYUN_SMS_ACCESS_KEY_SECRET=""
$env:ALIYUN_SMS_SIGN_NAME=""
```

邮箱：

```powershell
$env:SMTP_HOST=""
$env:SMTP_USERNAME=""
$env:SMTP_PASSWORD=""
```

微信开放平台：

```powershell
$env:WECHAT_OPEN_CLIENT_ID=""
$env:WECHAT_OPEN_CLIENT_SECRET=""
```

微博 OAuth：

```powershell
$env:WEIBO_CLIENT_ID=""
$env:WEIBO_CLIENT_SECRET=""
```

腾讯云实名/人脸：

```powershell
$env:TENCENT_FACE_SECRET_ID=""
$env:TENCENT_FACE_SECRET_KEY=""
```

对象存储：

```powershell
$env:OSS_ENDPOINT=""
$env:OSS_BUCKET=""
```

搜索与缓存：

```powershell
$env:FORUM_SEARCH_ENDPOINT=""
$env:FORUM_SEARCH_INDEX_PREFIX="forum"
$env:FORUM_REDIS_URL=""
```

## 常见问题

### 1. 端口 8080 被占用

错误：

```text
Port 8080 was already in use.
```

查看占用进程：

```powershell
Get-NetTCPConnection -LocalPort 8080 | Select-Object LocalAddress,LocalPort,State,OwningProcess
```

停止进程：

```powershell
Stop-Process -Id 进程ID -Force
```

或者换端口：

```powershell
$env:SERVER_PORT="8081"
mvn spring-boot:run
```

换端口后记得修改前端 `vite.config.js` 的代理地址。

### 2. MySQL 没有创建表

先确认启动时没有带：

```powershell
-Dspring-boot.run.profiles=test
```

带了 `test` 就会使用 H2，不会写入 MySQL。

再确认 `MYSQL_URL` 指向正确数据库：

```powershell
$env:MYSQL_URL
```

应该类似：

```text
jdbc:mysql://localhost:3306/standalone?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
```

启动日志中应该看到：

```text
Database: jdbc:mysql://localhost:3306/standalone
Migrating schema ... to version "1 - init"
Migrating schema ... to version "2 - forum"
```

### 3. Maven 缓存损坏

错误：

```text
org/apache/commons/io/FilenameUtils
```

使用项目内 Maven 仓库：

```powershell
mvn "-Dmaven.repo.local=D:\medicine-assistant\medicine-assistant\stock-forum-standalone\backend\.m2\repository" spring-boot:run
```

### 4. 前端请求接口失败

确认后端已启动：

```text
http://localhost:8080/api/forum/boards
```

确认前端代理地址：

```text
frontend/vite.config.js
```

默认应代理到：

```text
http://localhost:8080
```

### 5. PowerShell 无法运行 npm

如果遇到 `npm.ps1` 执行策略问题，使用：

```powershell
npm.cmd run dev:h5
```

不要直接使用：

```powershell
npm run dev:h5
```

## 生产部署提醒

- 不要使用默认 `JWT_SECRET`
- MySQL 必须使用强密码和独立账号
- 生产环境不要使用 H2
- 第三方登录、短信、人脸、对象存储需要配置真实服务商密钥
- 内容合规规则上线前需要人工复核
- 当前搜索和热榜使用数据库降级实现，如有高并发需要接入 Redis 和 Elasticsearch/OpenSearch

