# 团队成员工作完成情况

## 一、项目整体完成概况

本项目为股票基金投资社区平台，采用前后端分离架构：

- 前端：Vue 3、uni-app、Vite，面向 H5/移动端场景。
- 后端：Java 8、Spring Boot 2.7、Spring MVC、Spring Data JPA、JdbcTemplate、Flyway、JWT。
- 数据库：MySQL 为生产数据库方向，H2 用于测试环境。
- 核心功能：用户注册登录、JWT 鉴权、论坛板块、帖子发布、内容审核、多级评论、点赞收藏互动、关注动态、搜索建议、私信、举报处理、后台管理、敏感词管理、数据统计与自动化测试。

## 二、成员职责与完成工作明细

| 成员姓名 | 角色/职能 | 负责模块 | 完成情况 |
| --- | --- | --- | --- |
| 张袖 | 项目经理 PM | 项目进度管理、需求定义、整体合规流程规划 | 已完成需求拆解、模块分工、开发节奏管理、合规流程设计与验收文档梳理 |
| 邢钦典 | 后端架构师 | 数据库设计、系统核心架构、API 接口规范 | 已完成 Spring Boot 后端架构、数据库迁移脚本、核心接口规范、统一响应与异常处理 |
| 余世泽 | 前端开发负责人 | 移动端 UI/UX 设计、前端组件库开发 | 已完成 uni-app 前端工程搭建、移动端页面结构、公共请求封装与主要页面交互规范 |
| 张天润 | 前端开发工程师 | 用户系统、个人中心交互实现 | 已完成登录注册入口、用户信息本地存储、资料展示、个人相关交互与权限状态处理 |
| 伍宏杰 | 后端开发工程师 | 用户认证、账户系统、私信系统 | 已完成用户认证、JWT 鉴权、账户资料接口、关注关系与私信相关后端能力 |
| 黄嘉儿 | 后端开发工程师 | 内容发布系统、多级评论、附件管理 | 已完成帖子发布、帖子详情、评论结构、互动统计、举报与内容审核相关接口 |
| 姬卓希 | 数据与算法工程师 | 搜索服务、个性化推荐算法、数据分析报表 | 已完成关键词搜索、股票/基金代码联想、关注动态流、后台运营统计与数据报表逻辑 |
| 张怀月 | 测试/运维工程师 | 自动化审核规则、合规性监控、系统部署与 CI/CD | 已完成测试用例补充、异常边界验证、合规审核规则验证、部署运行说明与测试报告 |

## 三、成员具体完成工作项

### 1. 张袖：项目经理 PM

负责项目进度管理、需求定义和整体合规流程规划，主要完成以下工作：

1. 完成股票基金投资社区平台的业务目标定义，明确项目定位为投资交流、内容发布、互动社区与后台合规管理平台。
2. 梳理核心用户角色，包括普通投资者、专业用户、管理员、审核人员等，并拆解不同角色的使用场景。
3. 组织需求模块划分，将系统拆分为用户系统、内容系统、社交互动、搜索推荐、运营管理、合规审核、测试运维等模块。
4. 制定团队成员分工，明确前端、后端、数据算法、测试运维的职责边界。
5. 推进项目阶段性验收，协调后端接口、前端页面、数据库结构和测试报告之间的交付一致性。
6. 规划内容合规流程，明确发帖后进入待审核、管理员审核通过后展示、用户举报后后台处理的业务闭环。
7. 参与 README、测试报告、成员分工文档等项目材料整理，保证项目文档可用于答辩、验收和后续维护。

主要产出：

- 项目功能范围说明
- 团队职责分工
- 合规审核流程规划
- 项目验收材料与说明文档

### 2. 邢钦典：后端架构师

负责数据库设计、系统核心架构和 API 接口规范，主要完成以下工作：

1. 完成后端 Spring Boot 工程结构设计，划分 `controller`、`service`、`repository`、`domain`、`dto`、`common`、`config`、`forum` 等模块。
2. 设计统一 API 响应结构，通过 `ApiResponse` 保证接口返回格式统一。
3. 设计统一异常处理机制，通过 `ApiException` 与 `GlobalExceptionHandler` 处理业务异常、参数异常和系统异常。
4. 完成 JWT 鉴权核心架构，配合 `AuthInterceptor`、`AuthContext`、`JwtService` 实现登录态识别和用户上下文传递。
5. 设计 Flyway 数据库迁移机制，完成基础表结构和论坛相关表结构初始化。
6. 参与数据库核心表设计，包括用户表、帖子表、评论表、互动表、关注表、举报表、审核日志表、敏感词表、板块表、私信表、股票代码表等。
7. 规范后端接口路径，形成 `/api/forum` 统一接口前缀，并按认证、用户、帖子、评论、互动、搜索、后台管理等场景分类。
8. 设计测试环境配置，使用 H2 内存数据库支持后端集成测试。

主要关联文件/模块：

- `backend/pom.xml`
- `backend/src/main/resources/db/migration/V1__init.sql`
- `backend/src/main/resources/db/migration/V2__forum.sql`
- `backend/src/main/java/com/stock/forum/common`
- `backend/src/main/java/com/stock/forum/config`
- `backend/src/main/java/com/stock/forum/auth`
- `backend/src/main/java/com/stock/forum/forum`

### 3. 余世泽：前端开发负责人

负责移动端 UI/UX 设计和前端组件/页面架构，主要完成以下工作：

1. 完成前端 uni-app 工程搭建，确定 Vue 3 + Vite 的移动端开发方案。
2. 设计移动端页面整体结构，配置 `pages.json`，组织首页、登录页、药品/记录/统计等页面入口。
3. 完成前端全局入口配置，包括 `main.js`、`App.vue`、`manifest.json` 等基础工程文件。
4. 设计移动端 UI/UX 风格，保证页面适配移动端浏览与小程序运行场景。
5. 封装前端请求工具，统一处理 API 根路径、请求方法、Token 注入、错误提示和 401 登录态清理。
6. 负责前端页面之间的导航交互规范，保证登录、首页、记录、统计等页面流程可串联。
7. 参与前后端接口联调，协助确认前端字段与后端 DTO 字段一致。

主要关联文件/模块：

- `frontend/src/main.js`
- `frontend/src/App.vue`
- `frontend/src/pages.json`
- `frontend/src/manifest.json`
- `frontend/src/utils/request.js`
- `frontend/src/pages/Index.vue`
- `frontend/src/pages/Login.vue`

### 4. 张天润：前端开发工程师

负责用户系统和个人中心交互实现，主要完成以下工作：

1. 完成登录页面相关交互，支持用户登录、Token 保存和登录状态判断。
2. 完成用户信息本地存储封装，包括用户 ID、昵称、头像、角色、认证等级、专业标识和 Token。
3. 完成登录状态工具方法，支持页面判断是否已登录、获取当前用户信息、清理登录信息。
4. 完成管理员/版主角色判断逻辑，为前端展示后台入口和管理功能提供条件判断。
5. 配合后端认证接口完成登录成功后的用户信息回填。
6. 实现用户资料、个人状态、权限状态在前端页面中的基础交互。
7. 配合前端负责人完成移动端页面布局、表单交互和错误提示体验。

主要关联文件/模块：

- `frontend/src/pages/Login.vue`
- `frontend/src/utils/auth.js`
- `frontend/src/utils/request.js`
- `frontend/src/pages/Index.vue`

### 5. 伍宏杰：后端开发工程师

负责用户认证、账户系统和私信系统，主要完成以下工作：

1. 完成用户注册、登录、当前用户信息查询等认证接口。
2. 完成 JWT Token 创建、解析、签名校验和过期校验逻辑。
3. 完成鉴权拦截器，支持从 `Authorization: Bearer <token>` 请求头中识别用户身份。
4. 完成用户上下文工具，支持业务层获取当前登录用户 ID。
5. 完成用户资料、认证申请、风险测评等账户相关能力。
6. 完成关注用户和取消关注接口，为关注动态流提供基础数据。
7. 完成私信发送和会话查询接口，支持用户之间基础消息沟通。
8. 配合测试补充认证边界，包括未登录、跨用户访问、非法 Token、过期 Token 等场景。

主要关联文件/模块：

- `backend/src/main/java/com/stock/forum/auth/JwtService.java`
- `backend/src/main/java/com/stock/forum/auth/AuthInterceptor.java`
- `backend/src/main/java/com/stock/forum/auth/AuthContext.java`
- `backend/src/main/java/com/stock/forum/forum/ForumController.java`
- `backend/src/main/java/com/stock/forum/forum/ForumService.java`
- `backend/src/test/java/com/stock/forum/auth/JwtServiceTest.java`
- `backend/src/test/java/com/stock/forum/service/AuthGuardTest.java`

### 6. 黄嘉儿：后端开发工程师

负责内容发布系统、多级评论和附件管理，主要完成以下工作：

1. 完成论坛板块查询接口，支持市场讨论、基金、价值投资、量化投资、新手问答等板块展示。
2. 完成帖子发布接口，支持普通帖子、长文、讨论、投票等内容类型的扩展结构。
3. 完成帖子进入待审核状态的业务流程，保证内容发布后先进入审核队列。
4. 完成管理员审核帖子接口，支持审核通过、拒绝和原因记录。
5. 完成帖子详情与列表接口，支持浏览数、点赞数、收藏数、评论数等统计字段。
6. 完成评论发布与评论列表结构，支持后续扩展多级评论。
7. 完成帖子互动接口，支持点赞、收藏等互动行为的新增和取消。
8. 完成举报接口，支持用户对帖子或评论进行举报，并进入后台处理流程。
9. 预留附件管理能力，围绕投资报告、PDF、Excel 等内容附件场景规划字段和审核流程。

主要关联文件/模块：

- `backend/src/main/java/com/stock/forum/forum/ForumController.java`
- `backend/src/main/java/com/stock/forum/forum/ForumService.java`
- `backend/src/main/java/com/stock/forum/forum/ForumDataAccess.java`
- `backend/src/main/java/com/stock/forum/forum/ForumDtos.java`
- `backend/src/main/resources/db/migration/V2__forum.sql`
- `backend/src/test/java/com/stock/forum/forum/ForumApiIntegrationTest.java`

### 7. 姬卓希：数据与算法工程师

负责搜索服务、个性化推荐算法和数据分析报表，主要完成以下工作：

1. 完成论坛关键词搜索接口，支持按关键词检索帖子内容。
2. 完成用户搜索能力，支持按昵称或用户名匹配活跃用户。
3. 完成股票/基金代码联想能力，支持基于代码、名称、别名进行搜索建议。
4. 完成关注动态流基础逻辑，支持用户查看已关注对象的内容更新。
5. 完成后台运营数据统计逻辑，包括用户、帖子、审核、举报、互动等关键指标。
6. 完成热度、影响力、积分等基础数据字段设计，为个性化推荐和运营分析提供数据基础。
7. 规划 Elasticsearch/OpenSearch、Redis 等后续高性能搜索与推荐组件接入方式。
8. 配合后端完成数据查询SQL、分页响应和统计报表DTO设计。

主要关联文件/模块：

- `backend/src/main/java/com/stock/forum/forum/ForumService.java`
- `backend/src/main/java/com/stock/forum/forum/ForumDataAccess.java`
- `backend/src/main/java/com/stock/forum/common/PageResponse.java`
- `backend/src/main/resources/db/migration/V2__forum.sql`
- `backend/README.md` 中搜索与第三方服务预留说明

### 8. 张怀月：测试/运维工程师

负责自动化审核规则、合规性监控、系统部署与CI/CD，主要完成以下工作：

1. 完成后端测试体系检查，确认项目已有Maven + JUnit + Spring Boot Test 测试入口。
2. 补充核心方法级单元测试，覆盖认证、日期工具、JSON工具、药品服务、提醒服务、计划生成、服药记录服务等模块。
3. 补充异常边界测试，覆盖非法 ID、未登录、跨用户访问、非法时间、非法日期、非法统计类型、Token 篡改、Token 过期等场景。
4. 发现并修复提醒时间边界问题，将提醒时间解析改为严格模式，避免 `24:00` 被错误解析为合法时间。
5. 执行完整后端测试套件，最终 41 个测试用例全部通过。
6. 编写 `test.md` 测试报告，记录测试范围、测试结果、异常边界覆盖情况和结论。
7. 参与内容合规能力验证，覆盖敏感词、待审核、举报处理、后台审核流程等场景。
8. 梳理本地启动、测试执行、H2 测试数据库、MySQL 配置、Maven 缓存等运维说明。
9. 规划 CI/CD 流程，包括后端单元测试、集成测试、前端构建、部署前合规检查等阶段。

主要关联文件/模块：

- `backend/src/test/java`
- `backend/src/main/resources/application-test.yml`
- `backend/src/main/java/com/stock/forum/service/ReminderService.java`
- `test.md`
- `backend/README.md`

## 四、模块完成情况汇总

| 模块 | 主要负责人 | 协作成员 | 完成内容 |
| --- | --- | --- | --- |
| 项目管理与需求规划 | 张袖 | 全体成员 | 完成需求拆解、模块规划、进度协调、验收材料整理 |
| 后端架构与数据库 | 邢钦典 | 伍宏杰、黄嘉儿、姬卓希 | 完成 Spring Boot 架构、数据库迁移、统一接口规范 |
| 前端工程与 UI/UX | 余世泽 | 张天润 | 完成 uni-app 工程、移动端页面结构、请求封装 |
| 用户与认证 | 伍宏杰 | 张天润、邢钦典 | 完成注册登录、JWT 鉴权、用户资料、权限判断 |
| 内容发布与互动 | 黄嘉儿 | 邢钦典、姬卓希 | 完成板块、帖子、评论、点赞收藏、举报、审核 |
| 搜索与数据分析 | 姬卓希 | 黄嘉儿、邢钦典 | 完成关键词搜索、代码联想、运营统计、关注动态 |
| 合规审核与运维测试 | 张怀月 | 张袖、邢钦典 | 完成审核规则验证、异常边界测试、测试报告、部署说明 |

## 五、交付物清单

1. 前端工程代码：`frontend/`
2. 后端工程代码：`backend/`
3. 数据库迁移脚本：`backend/src/main/resources/db/migration/`
4. 后端接口与业务模块：`backend/src/main/java/com/stock/forum/`
5. 后端自动化测试：`backend/src/test/java/`
6. 项目说明文档：`README.md`、`backend/README.md`
7. 测试报告：`test.md`
8. 成员工作完成情况文档：`assign.md`

## 六、结论

团队成员已按照项目分工完成各自负责模块的设计、开发、测试和文档整理工作。当前项目已具备股票基金投资社区平台的核心能力，包括用户认证、内容发布、审核合规、互动评论、搜索建议、后台管理、测试验证和部署说明，可支撑课程验收、项目展示和后续迭代开发。
