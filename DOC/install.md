# 软件安装文档 (Installation Guide) - 股票基金投资论坛

---

## 1. 系统要求

### 1.1 硬件要求
| 组件 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 双核 2.0GHz | 四核 2.5GHz+ |
| 内存 | 4 GB | 8 GB+ |
| 磁盘 | 10 GB 可用空间 | 20 GB+ SSD |
| 网络 | 宽带互联网连接 | 宽带互联网连接 |

### 1.2 软件要求

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | **1.8+** (Java 8) | 后端运行环境 |
| Maven | 3.6+ | 后端构建工具 |
| MySQL | 8.0+ | 生产数据库 |
| Node.js | 16+ | 前端构建环境 |
| npm | 8+ | 前端包管理器 |

---

## 2. 环境准备

### 2.1 安装 JDK 8

**Windows**:
1. 下载 [JDK 8](https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html)
2. 运行安装程序
3. 配置环境变量:
   ```cmd
   setx JAVA_HOME "C:\Program Files\Java\jdk1.8.0_xx"
   setx PATH "%PATH%;%JAVA_HOME%\bin"
   ```
4. 验证安装: `java -version`

**macOS**:
```bash
brew install openjdk@8
export JAVA_HOME=/usr/local/opt/openjdk@8
java -version
```

**Linux**:
```bash
sudo apt install openjdk-8-jdk
java -version
```

### 2.2 安装 Maven

**Windows**:
1. 下载 [Maven](https://maven.apache.org/download.cgi)
2. 解压到 `C:\apache-maven-3.x`
3. 配置环境变量:
   ```cmd
   setx MAVEN_HOME "C:\apache-maven-3.x"
   setx PATH "%PATH%;%MAVEN_HOME%\bin"
   ```
4. 验证安装: `mvn -version`

**macOS**: `brew install maven`
**Linux**: `sudo apt install maven`

### 2.3 安装 MySQL

**Windows**:
1. 下载 [MySQL Installer](https://dev.mysql.com/downloads/installer/)
2. 安装 MySQL Server 8.0+
3. 设置 root 用户密码
4. 创建数据库:
```sql
CREATE DATABASE medicine_assistant
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

**macOS**: `brew install mysql`
**Linux**: `sudo apt install mysql-server`

### 2.4 安装 Node.js

**Windows/macOS/Linux**:
1. 下载 [Node.js 16+](https://nodejs.org/)
2. 运行安装程序
3. 验证安装: `node -v && npm -v`

---

## 3. 后端安装配置

### 3.1 获取源码
```bash
git clone https://github.com/everyonewoojia/StockInvestmentForum.git
cd StockInvestmentForum
```

### 3.2 配置数据库连接

编辑 `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: ${MYSQL_URL:jdbc:mysql://localhost:3306/medicine_assistant?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false}
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:your_password}
```

或者通过环境变量配置:
```bash
set MYSQL_URL=jdbc:mysql://localhost:3306/medicine_assistant?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
set MYSQL_USER=root
set MYSQL_PASSWORD=your_password
set JWT_SECRET=your-custom-jwt-secret-key
```

### 3.3 配置外部服务 (可选)

外部的服务均为可选配置，采用默认占位值即可运行核心功能:

```yaml
app:
  oss:
    endpoint: ${OSS_ENDPOINT:https://oss-cn-hangzhou.aliyuncs.com}
    bucket: ${OSS_BUCKET:default}
    access-key: ${OSS_ACCESS_KEY:default}
    secret-key: ${OSS_SECRET_KEY:default}
  sms:
    access-key: ${SMS_ACCESS_KEY:default}
    secret-key: ${SMS_SECRET_KEY:default}
    sign-name: ${SMS_SIGN_NAME:default}
    template-code: ${SMS_TEMPLATE_CODE:default}
  wechat:
    appid: ${WECHAT_APPID:default}
    secret: ${WECHAT_SECRET:default}
  tencent-ocr:
    secret-id: ${TENCENT_OCR_SECRET_ID:default}
    secret-key: ${TENCENT_OCR_SECRET_KEY:default}
    region: ${TENCENT_OCR_REGION:ap-guangzhou}
```

### 3.4 构建与运行

```bash
# 进入后端目录
cd backend

# 编译打包 (跳过测试)
mvn clean package -DskipTests

# 运行
java -jar target/medicine-assistant-1.0.0.jar

# 或者使用开发模式
mvn spring-boot:run
```

### 3.5 运行测试 (可选)
```bash
mvn test
```

### 3.6 验证后端运行
```bash
# 健康检查
curl http://localhost:8080/api/forum/boards

# 预期返回板块 JSON 列表
```

---

## 4. 前端安装配置

### 4.1 安装依赖
```bash
cd frontend
npm install
```

### 4.2 配置 API 代理

`frontend/vite.config.js` 已默认配置 API 代理:
```js
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

如后端地址不同，修改 `target` 为目标地址。

### 4.3 运行开发服务器
```bash
npm run dev
```

### 4.4 构建生产版本
```bash
npm run build
```
构建产物在 `frontend/dist/` 目录。

### 4.5 访问应用
打开浏览器访问: `http://localhost:5173`

---

## 5. 演示账号

系统启动后自动创建三个演示用户:

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| `admin` | `forum-admin-2026` | ADMIN | 管理员，可访问管理后台 |
| `analyst` | `forum-analyst-2026` | USER | 分析师用户 |
| `investor` | `forum-investor-2026` | USER | 普通投资者 |

---

## 6. Docker 部署 (可选)

### 6.1 使用 Docker Compose

创建 `docker-compose.yml`:

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: medicine_assistant
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      MYSQL_URL: jdbc:mysql://mysql:3306/medicine_assistant?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
      MYSQL_USER: root
      MYSQL_PASSWORD: root123
      JWT_SECRET: docker-jwt-secret
    depends_on:
      - mysql

  frontend:
    build: ./frontend
    ports:
      - "5173:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

### 6.2 构建与启动
```bash
docker-compose up -d
```

---

## 7. 常见安装问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| `java: 未找到命令` | JDK 未安装或未配置环境变量 | 安装 JDK 8+ 并设置 JAVA_HOME |
| `mvn: 未找到命令` | Maven 未安装或未配置 | 安装 Maven 并设置 MAVEN_HOME |
| `Access denied for user 'root'@'localhost'` | MySQL 密码错误 | 检查 application.yml 中的数据库密码 |
| `Unknown database 'medicine_assistant'` | 数据库未创建 | 执行 `CREATE DATABASE medicine_assistant` |
| `npm ERR!` 依赖安装失败 | 网络问题或 Node 版本不匹配 | 检查 Node.js 版本 (16+) 或使用镜像源 |
| `端口 8080 被占用` | 其他进程在使用端口 | 修改 application.yml 中 `server.port` 或关闭占用进程 |
| Flyway 迁移失败 | 数据库表结构冲突 | 检查数据库是否有已存在的表，或清空数据库重试 |

---

## 8. 端口说明

| 端口 | 服务 | 说明 |
|------|------|------|
| 8080 | 后端 (Spring Boot) | API 服务端口 |
| 5173 | 前端 (Vite) | 开发服务器端口 |
| 3306 | MySQL | 数据库端口 |
