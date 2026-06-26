# 股票基金投资论坛后端接口文档

## 1. 接口约定

服务根路径：

```text
/api/forum
```

认证方式：

```http
Authorization: Bearer <token>
```

统一响应结构：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

分页响应结构：

```json
{
  "records": [],
  "total": 0,
  "page": 1,
  "size": 10
}
```

业务错误响应：

```json
{
  "code": 400,
  "msg": "错误信息",
  "data": null
}
```

业务错误码：

| code | 含义 |
| ---: | --- |
| 200 | 请求成功 |
| 400 | 请求参数或业务规则校验失败 |
| 401 | 未登录、Token 无效或权限不足 |
| 500 | 服务端处理异常 |

## 2. 枚举值

用户角色：

| 值 | 说明 |
| --- | --- |
| `USER` | 普通用户 |
| `PRO_USER` | 专业用户 |
| `MODERATOR` | 版主/运营 |
| `ADMIN` | 管理员 |

认证等级：

| 值 | 说明 |
| --- | --- |
| `BASIC` | 基础认证 |
| `REAL_NAME` | 实名认证 |
| `PROFESSIONAL` | 专业认证 |

内容状态：

| 值 | 说明 |
| --- | --- |
| `DRAFT` | 草稿 |
| `PENDING_REVIEW` | 待审核 |
| `PUBLISHED` | 已发布 |
| `REJECTED` | 已拒绝 |
| `DELETED` | 已删除 |

帖子类型：

| 值 | 说明 |
| --- | --- |
| `NORMAL` | 普通帖子 |
| `LONG_ARTICLE` | 长文分析 |
| `POLL` | 投票调研 |
| `SHORT` | 实时短讨论 |

互动类型：

| 值 | 说明 |
| --- | --- |
| `LIKE` | 点赞 |
| `FAVORITE` | 收藏 |
| `SHARE` | 转发 |

举报状态：

| 值 | 说明 |
| --- | --- |
| `OPEN` | 待处理 |
| `RESOLVED` | 已处理 |

## 3. 认证接口

### 3.1 注册

```http
POST /api/forum/auth/register
```

请求体：

```json
{
  "username": "investor",
  "phone": "13800000000",
  "email": "investor@example.com",
  "password": "investor123",
  "nickName": "长期主义者"
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 条件必填 | 用户名，用户名/手机号/邮箱至少提供一项 |
| phone | string | 条件必填 | 手机号 |
| email | string | 条件必填 | 邮箱 |
| password | string | 是 | 密码，至少 6 位 |
| nickName | string | 否 | 昵称 |

响应：

```json
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "userId": "1",
    "nickName": "长期主义者",
    "avatarUrl": "",
    "token": "jwt-token",
    "role": "USER",
    "verificationLevel": "BASIC",
    "professionalBadge": false
  }
}
```

### 3.2 登录

```http
POST /api/forum/auth/login
```

请求体：

```json
{
  "account": "admin",
  "password": "forum-admin-2026"
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| account | string | 是 | 用户名、手机号或邮箱 |
| password | string | 是 | 密码 |

响应数据结构同注册接口。

### 3.3 获取当前用户

```http
GET /api/forum/auth/me
Authorization: Bearer <token>
```

响应：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickName": "运营管理员",
    "role": "ADMIN",
    "verificationLevel": "PROFESSIONAL",
    "professionalBadge": true,
    "postCount": 3,
    "followerCount": 0,
    "followingCount": 0
  }
}
```

## 4. 用户资料与认证接口

### 4.1 更新个人资料

```http
PUT /api/forum/users/me
Authorization: Bearer <token>
```

请求体：

```json
{
  "nickName": "北向研究员",
  "avatarUrl": "https://example.com/avatar.png",
  "bio": "关注指数基金和行业轮动",
  "experienceTags": ["ETF", "价值投资"],
  "markets": ["A股", "基金"],
  "riskPreference": "BALANCED",
  "privacyProfile": "PUBLIC"
}
```

响应：更新后的用户资料。

### 4.2 提交认证申请

```http
POST /api/forum/users/me/verifications
Authorization: Bearer <token>
```

请求体：

```json
{
  "type": "PROFESSIONAL",
  "realName": "张三",
  "idNumber": "110101199001011234",
  "provider": "tencent-faceid",
  "externalRequestId": "faceid-request-id",
  "materials": ["qualification.pdf", "degree.pdf"]
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| type | string | 是 | `BASIC`、`REAL_NAME`、`PROFESSIONAL` |
| realName | string | 否 | 真实姓名 |
| idNumber | string | 否 | 身份证号 |
| provider | string | 否 | 认证服务商 |
| externalRequestId | string | 否 | 外部认证流水号 |
| materials | array | 否 | 认证材料 URL 或文件标识 |

响应：更新后的用户资料。

### 4.3 完成风险评估

```http
POST /api/forum/users/me/risk-assessment
Authorization: Bearer <token>
```

请求体：

```json
{
  "score": 72,
  "riskLevel": "BALANCED",
  "answers": ["长期投资", "可承受中等波动", "了解基金和股票基础风险"]
}
```

响应：更新后的用户资料。

## 5. 板块接口

### 5.1 查询板块

```http
GET /api/forum/boards?includeDisabled=false
```

查询参数：

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| includeDisabled | boolean | false | 是否包含停用板块 |

响应：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "name": "A股市场",
      "slug": "a-share",
      "category": "市场讨论区",
      "description": "沪深京市场热点、个股和交易制度讨论",
      "market": "A股",
      "sortOrder": 10,
      "enabled": true
    }
  ]
}
```

## 6. 帖子接口

### 6.1 查询已发布帖子

```http
GET /api/forum/posts?boardId=1&keyword=ETF&page=1&size=10
```

查询参数：

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| boardId | number | 空 | 板块 ID |
| keyword | string | 空 | 关键词，匹配标题、正文和股票代码 |
| page | number | 1 | 页码 |
| size | number | 10 | 每页数量，最大 50 |

响应：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "authorId": 2,
        "authorName": "北向研究员",
        "boardId": 1,
        "boardName": "A股市场",
        "type": "LONG_ARTICLE",
        "title": "市场缩量时如何观察行业轮动",
        "summary": "从成交额、估值分位和业绩预期三个维度观察轮动。",
        "status": "PUBLISHED",
        "likeCount": 0,
        "favoriteCount": 0,
        "shareCount": 0,
        "commentCount": 0,
        "viewCount": 1
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### 6.2 查询我的帖子

```http
GET /api/forum/posts/mine?page=1&size=10
Authorization: Bearer <token>
```

响应：分页帖子列表。

### 6.3 创建帖子

```http
POST /api/forum/posts
Authorization: Bearer <token>
```

请求体：

```json
{
  "boardId": 1,
  "type": "LONG_ARTICLE",
  "title": "ETF配置检查清单",
  "summary": "费率、跟踪误差、规模和流动性是基础指标。",
  "content": "正文内容",
  "stockCodes": ["510300"],
  "images": ["https://example.com/image.png"],
  "attachments": ["report.pdf"]
}
```

响应：创建后的帖子，默认进入 `PENDING_REVIEW`。

### 6.4 查询帖子详情

```http
GET /api/forum/posts/{postId}
Authorization: Bearer <token>
```

路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| postId | number | 帖子 ID |

响应：帖子详情，包含评论列表和当前用户互动状态。

### 6.5 查询评论

```http
GET /api/forum/posts/{postId}/comments
```

响应：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "postId": 1,
      "userId": 3,
      "parentId": null,
      "replyToId": null,
      "content": "这份清单很实用",
      "status": "PUBLISHED",
      "authorName": "普通投资者"
    }
  ]
}
```

### 6.6 创建评论

```http
POST /api/forum/posts/{postId}/comments
Authorization: Bearer <token>
```

请求体：

```json
{
  "parentId": null,
  "replyToId": null,
  "content": "这份清单很实用"
}
```

响应：创建后的评论。

### 6.7 点赞、收藏、转发

```http
POST /api/forum/posts/{postId}/interactions
Authorization: Bearer <token>
```

请求体：

```json
{
  "action": "LIKE",
  "active": true
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| action | string | 是 | `LIKE`、`FAVORITE`、`SHARE` |
| active | boolean | 否 | true 表示激活，false 表示取消 |

响应：更新后的帖子。

## 7. 举报接口

### 7.1 提交举报

```http
POST /api/forum/reports
Authorization: Bearer <token>
```

请求体：

```json
{
  "targetType": "POST",
  "targetId": 1,
  "reason": "内容可能存在合规风险",
  "detail": "用户提交的补充说明"
}
```

响应：创建后的举报记录。

## 8. 社交接口

### 8.1 关注用户

```http
POST /api/forum/social/follow/{targetUserId}?starred=false
Authorization: Bearer <token>
```

路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| targetUserId | number | 被关注用户 ID |

查询参数：

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| starred | boolean | false | 是否特别关注 |

响应：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "success": true
  }
}
```

### 8.2 取消关注

```http
DELETE /api/forum/social/follow/{targetUserId}
Authorization: Bearer <token>
```

响应：操作结果。

### 8.3 关注动态

```http
GET /api/forum/social/following-feed?page=1&size=10
Authorization: Bearer <token>
```

响应：分页帖子列表。

## 9. 搜索接口

### 9.1 综合搜索

```http
GET /api/forum/search?keyword=ETF&page=1&size=10
```

响应：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "posts": {
      "records": [],
      "total": 0,
      "page": 1,
      "size": 10
    },
    "users": [],
    "symbols": [],
    "engine": "jdbc-fallback"
  }
}
```

### 9.2 股票代码联想

```http
GET /api/forum/search/suggest?keyword=510300
```

响应：股票/基金代码联想列表。

## 10. 群组接口

### 10.1 查询公开群组

```http
GET /api/forum/groups
```

响应：公开群组列表。

### 10.2 创建群组

```http
POST /api/forum/groups
Authorization: Bearer <token>
```

请求体：

```json
{
  "name": "ETF研究小组",
  "description": "指数基金研究与资料分享",
  "visibility": "PUBLIC",
  "joinPolicy": "OPEN"
}
```

响应：创建后的群组。

### 10.3 加入群组

```http
POST /api/forum/groups/{groupId}/join
Authorization: Bearer <token>
```

响应：操作结果。

## 11. 私信接口

### 11.1 发送私信

```http
POST /api/forum/messages
Authorization: Bearer <token>
```

请求体：

```json
{
  "receiverId": 2,
  "content": "你好，想交流一下ETF配置。",
  "imageUrl": "https://example.com/chat.png"
}
```

响应：创建后的私信。

### 11.2 查询会话消息

```http
GET /api/forum/messages/{peerId}
Authorization: Bearer <token>
```

响应：与指定用户的会话消息列表。

## 12. 管理后台接口

管理员接口需要 `ADMIN` 或 `MODERATOR` 角色。

### 12.1 仪表盘

```http
GET /api/forum/admin/dashboard
Authorization: Bearer <admin-token>
```

响应：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "users": 3,
    "posts": 3,
    "publishedPosts": 3,
    "pendingPosts": 0,
    "openReports": 0,
    "hotBoards": [],
    "hotPosts": []
  }
}
```

### 12.2 查询待审核帖子

```http
GET /api/forum/admin/review/posts?status=PENDING_REVIEW&page=1&size=20
Authorization: Bearer <admin-token>
```

响应：分页帖子列表。

### 12.3 审核帖子

```http
POST /api/forum/admin/review/posts/{postId}
Authorization: Bearer <admin-token>
```

请求体：

```json
{
  "decision": "APPROVE",
  "reason": "内容合规"
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| decision | string | 是 | `APPROVE` 或 `REJECT` |
| reason | string | 否 | 审核原因 |

响应：审核后的帖子。

### 12.4 查询举报

```http
GET /api/forum/admin/reports?status=OPEN&page=1&size=20
Authorization: Bearer <admin-token>
```

响应：分页举报列表。

### 12.5 处理举报

```http
POST /api/forum/admin/reports/{reportId}/resolve
Authorization: Bearer <admin-token>
```

请求体：

```json
{
  "decision": "APPROVE",
  "reason": "已处理"
}
```

响应：操作结果。

### 12.6 查询用户

```http
GET /api/forum/admin/users?keyword=investor&page=1&size=20
Authorization: Bearer <admin-token>
```

响应：分页用户列表。

### 12.7 违规处理

```http
POST /api/forum/admin/users/{userId}/violation
Authorization: Bearer <admin-token>
```

请求体：

```json
{
  "action": "WARN",
  "reason": "违反社区规范",
  "days": 7
}
```

字段说明：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| action | string | 是 | `WARN`、`MUTE`、`BAN` |
| reason | string | 否 | 处理原因 |
| days | number | 否 | 处理天数 |

响应：操作结果。

### 12.8 新增板块

```http
POST /api/forum/admin/boards
Authorization: Bearer <admin-token>
```

请求体：

```json
{
  "name": "期货讨论",
  "slug": "futures",
  "category": "市场讨论区",
  "description": "期货市场交流",
  "market": "期货",
  "sortOrder": 110,
  "enabled": true
}
```

响应：创建后的板块。

### 12.9 更新板块

```http
PUT /api/forum/admin/boards/{boardId}
Authorization: Bearer <admin-token>
```

请求体同新增板块。

响应：更新后的板块。

### 12.10 停用板块

```http
DELETE /api/forum/admin/boards/{boardId}
Authorization: Bearer <admin-token>
```

响应：操作结果。

### 12.11 查询敏感词

```http
GET /api/forum/admin/sensitive-words
Authorization: Bearer <admin-token>
```

响应：敏感词列表。

### 12.12 新增敏感词

```http
POST /api/forum/admin/sensitive-words
Authorization: Bearer <admin-token>
```

请求体：

```json
{
  "word": "稳赚",
  "category": "合规风险",
  "enabled": true
}
```

响应：创建后的敏感词。

### 12.13 删除敏感词

```http
DELETE /api/forum/admin/sensitive-words/{wordId}
Authorization: Bearer <admin-token>
```

响应：操作结果。
