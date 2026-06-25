# 股票基金投资论坛测试方案

## 1. 目标

本测试方案用于验证股票基金投资论坛网页端产品的核心业务闭环、异常边界、权限控制、数据访问、页面交互入口和端到端验收流程。测试体系由后端 JUnit 测试、MockMvc 接口测试、JDBC 数据访问测试、前端页面契约测试和 H5 构建验证组成。

## 2. 测试资产

| 层级 | 文件 | 用例数 | 验证重点 |
| --- | --- | ---: | --- |
| 后端单元测试 | `backend/src/test/java/com/stock/forum/auth/JwtServiceTest.java` | 4 | JWT 创建、解析、非法格式、签名篡改、过期 token |
| 后端单元测试 | `backend/src/test/java/com/stock/forum/common/CommonModelsTest.java` | 4 | `ApiResponse`、`PageResponse`、`ApiException` 统一模型 |
| 后端服务测试 | `backend/src/test/java/com/stock/forum/service/ForumServiceUnitTest.java` | 5 | 注册校验、发帖板块校验、关注自己、管理员权限 |
| 后端数据访问测试 | `backend/src/test/java/com/stock/forum/repository/ForumDataAccessTest.java` | 2 | JDBC 查询、插入主键、字段驼峰映射、时间格式化、计数与更新 |
| 后端接口测试 | `backend/src/test/java/com/stock/forum/controller/ForumApiExceptionTest.java` | 14 | 注册、登录、鉴权、发帖、评论、举报、管理员权限、非法 JSON、错误方法 |
| 后端集成测试 | `backend/src/test/java/com/stock/forum/controller/ForumApiIntegrationTest.java` | 1 | 注册、发帖、审核、评论、点赞、举报、搜索核心链路 |
| 后端端到端验收 | `backend/src/test/java/com/stock/forum/controller/ForumApiEndToEndAcceptanceTest.java` | 1 | 用户、管理员、关注、群组、私信、举报处理完整验收链路 |
| 前端页面契约测试 | `frontend/tests/pages.test.mjs` | 6 | 登录注册入口、第三方登录图标、发帖、帖子详情、互动、关注、私信、群组、后台审核入口 |

测试总量：后端 31 个用例，前端 6 个用例，总计 37 个用例。

## 3. 后端测试设计

### 3.1 认证与 JWT

验证内容：

- 正常创建 token 并解析用户 ID。
- 非 JWT 字符串返回 `401 / Invalid token`。
- 篡改签名返回 `401 / Invalid token`。
- 过期 token 返回 `401 / Token expired`。

对应文件：

```text
backend/src/test/java/com/stock/forum/auth/JwtServiceTest.java
```

### 3.2 统一响应与分页模型

验证内容：

- 成功响应固定返回 `code=200`。
- 失败响应携带业务错误码、错误消息和空数据体。
- 分页响应保留 `records / total / page / size`。
- `ApiException` 工厂方法固定输出 `400 / 401 / 500`。

对应文件：

```text
backend/src/test/java/com/stock/forum/common/CommonModelsTest.java
```

### 3.3 服务层业务边界

验证内容：

- 注册时用户名、手机号、邮箱至少填写一项。
- 注册密码长度至少 6 位。
- 发帖时板块必须存在。
- 用户不能关注自己。
- 普通用户不能访问管理员仪表盘。

对应文件：

```text
backend/src/test/java/com/stock/forum/service/ForumServiceUnitTest.java
```

### 3.4 数据访问层

验证内容：

- `insert` 返回数据库生成主键。
- SQL 查询结果字段从下划线转为驼峰，例如 `user_name` 转为 `userName`。
- `TIMESTAMP` 字段统一格式化为 `yyyy-MM-dd HH:mm:ss`。
- `count / update / query / queryOne` 与 JDBC 行为一致。
- 空查询返回 `Optional.empty()`。

对应文件：

```text
backend/src/test/java/com/stock/forum/repository/ForumDataAccessTest.java
```

## 4. 接口异常边界测试

接口异常测试统一通过 MockMvc 访问 `/api/forum`，响应 HTTP 状态为 `200`，业务结果通过响应体 `code` 判定。

### 4.1 请求与认证边界

验证内容：

- 注册时缺少用户名、手机号、邮箱。
- 注册密码不足 6 位。
- 注册重复用户名。
- 登录密码错误。
- 未携带 token 访问 `/auth/me`。
- 未携带 token 创建帖子。
- 请求体 JSON 格式错误。
- 使用不支持的 HTTP 方法访问接口。

### 4.2 内容与互动边界

验证内容：

- 使用不存在的板块 ID 创建帖子。
- 匿名访问待审核帖子。
- 对待审核帖子发表评论。
- 举报时缺少目标 ID。
- 用户关注自己。

### 4.3 管理权限边界

验证内容：

- 普通用户访问管理员仪表盘返回 `401`。
- 管理员相关接口需要管理员或运营角色。

对应文件：

```text
backend/src/test/java/com/stock/forum/controller/ForumApiExceptionTest.java
```

## 5. 核心链路集成测试

核心链路集成测试验证论坛第一阶段主流程：

1. 查询板块。
2. 注册普通用户。
3. 普通用户创建帖子，帖子进入 `PENDING_REVIEW`。
4. 管理员登录。
5. 管理员审核帖子为 `PUBLISHED`。
6. 用户评论帖子。
7. 用户点赞帖子。
8. 用户举报帖子。
9. 用户按关键词搜索帖子。

对应文件：

```text
backend/src/test/java/com/stock/forum/controller/ForumApiIntegrationTest.java
```

## 6. 端到端验收测试

端到端验收测试验证用户端、社交互动和运营后台串联流程：

1. 作者用户注册。
2. 关注者用户注册。
3. 作者更新个人资料。
4. 作者完成风险评估。
5. 作者提交专业认证材料。
6. 作者创建公开群组。
7. 关注者加入群组。
8. 作者发布长文帖子。
9. 管理员审核通过帖子。
10. 关注者搜索帖子。
11. 关注者评论帖子。
12. 关注者点赞帖子。
13. 关注者特别关注作者。
14. 关注者查看关注动态。
15. 关注者向作者发送私信。
16. 关注者举报帖子。
17. 管理员处理举报。

对应文件：

```text
backend/src/test/java/com/stock/forum/controller/ForumApiEndToEndAcceptanceTest.java
```

## 7. 前端页面契约测试

前端页面契约测试使用 Node.js 内置测试运行器，不引入额外测试依赖。

### 7.1 登录页

验证内容：

- 页面存在登录入口。
- 页面存在注册入口。
- 页面存在公开内容浏览入口。
- 页面存在微信、微博、QQ、支付宝第三方登录图标。
- 页面存在第三方登录处理函数。

### 7.2 论坛首页

验证内容：

- 页面存在发帖表单、帖子类型、股票/基金代码输入和发布动作。
- 页面存在帖子详情视图。
- 页面存在点赞、收藏、评论、举报等帖子互动入口。
- 页面存在登录态保护逻辑。
- 页面存在关注动态、关注作者、私信、群组创建和群组加入入口。
- 页面存在后台审核、举报处理、板块管理入口。

对应文件：

```text
frontend/tests/pages.test.mjs
```

## 8. 测试数据与环境

后端测试使用 `test` Profile：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:stock_forum;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
  flyway:
    enabled: true
```

数据初始化来源：

- Flyway 脚本：`backend/src/main/resources/db/migration/V1__forum.sql`
- 服务启动种子数据：管理员、专业用户、普通用户和示例帖子
- 测试用例动态创建的唯一用户名、帖子、群组、举报和私信

管理员测试账号：

```text
admin / forum-admin-2026
```

测试环境要求：

- JDK 8+
- Maven 3.8+
- Node.js 18+
- 项目依赖已安装
- Maven 本地仓库具备读取和写入权限

## 9. 执行命令

### 9.1 后端完整测试

```powershell
cd D:\medicine-assistant\medicine-assistant\stock-forum-standalone\backend
mvn test
```

### 9.2 后端编译级验证

```powershell
cd D:\medicine-assistant\medicine-assistant\stock-forum-standalone\backend
mvn test-compile
```

### 9.3 前端页面契约测试

```powershell
cd D:\medicine-assistant\medicine-assistant\stock-forum-standalone\frontend
npm.cmd run test:pages
```

### 9.4 前端 H5 构建验证

```powershell
cd D:\medicine-assistant\medicine-assistant\stock-forum-standalone\frontend
npm.cmd run build:h5
```

## 10. 验收标准

后端验收标准：

- `mvn test` 执行完成，所有 JUnit 测试通过。
- 统一响应结构保持 `code / msg / data`。
- 业务异常通过响应体 `code` 返回 `400 / 401 / 500`。
- Flyway 能在 H2 MySQL 模式下初始化论坛表结构。
- MockMvc 核心链路和端到端验收链路全部通过。

前端验收标准：

- `npm.cmd run test:pages` 执行完成，所有页面契约测试通过。
- `npm.cmd run build:h5` 构建完成。
- 登录页保留登录、注册、第三方登录和公开浏览入口。
- 首页保留发帖、详情、互动、关注、群组、私信和后台审核入口。

## 11. 质量门禁

提交代码前执行：

```powershell
cd D:\medicine-assistant\medicine-assistant\stock-forum-standalone\backend
mvn test

cd D:\medicine-assistant\medicine-assistant\stock-forum-standalone\frontend
npm.cmd run test:pages
npm.cmd run build:h5
```


