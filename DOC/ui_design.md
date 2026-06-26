# 前端 UI 设计文档 (UI Design) - 股票基金投资论坛

---

## 1. 全局设计规范

### 1.1 色彩体系

| 用途 | 颜色值 | 说明 |
|------|--------|------|
| 页面背景 | `#f4f7f8` | 浅灰蓝色 |
| 卡片背景 | `#ffffff` | 白色 |
| 主色调 | `#4a90d9` | 蓝色 (按钮/链接) |
| 文字主色 | `#2c3e50` | 深蓝灰色 |
| 文字次要 | `#7f8c8d` | 灰色 |
| 成功 | `#27ae60` | 绿色 |
| 警告 | `#e67e22` | 橙色 |
| 危险 | `#e74c3c` | 红色 |
| 边框 | `#e0e6ed` | 浅灰色 |

### 1.2 字体与排版

- 字体栈: -apple-system, Helvetica Neue, Helvetica, Arial, sans-serif
- 标题: 18px, 16px, 14px (三级)
- 正文: 14px
- 辅助文字: 12px

### 1.3 间距与尺寸

- 卡片内边距: 15px
- 卡片间间距: 12px
- 圆角: 8px (卡片), 4px (按钮)
- 阴影: 0 2px 8px rgba(0,0,0,0.06)

---

## 2. 页面设计

### 2.1 首页 (Index.vue) — 主论坛外壳

**说明**: 本页面是一个 SPA 风格外壳，通过内部变量currentView切换不同视图。

#### 布局结构

![image-20260625211229285](C:\Users\86135\AppData\Roaming\Typora\typora-user-images\image-20260625211229285.png)

#### 视图 A: Feed 视图 (默认)

![image-20260625211316004](C:\Users\86135\AppData\Roaming\Typora\typora-user-images\image-20260625211316004.png)

**帖子卡片设计**:

![image-20260625211348962](C:\Users\86135\AppData\Roaming\Typora\typora-user-images\image-20260625211348962.png)

#### 视图 B: Boards 视图

![image-20260625211403337](C:\Users\86135\AppData\Roaming\Typora\typora-user-images\image-20260625211403337.png)

#### 视图 C: Following 视图

![image-20260625211427087](C:\Users\86135\AppData\Roaming\Typora\typora-user-images\image-20260625211427087.png)


#### 视图 D: Groups 视图


![image-20260625211515702](C:\Users\86135\AppData\Roaming\Typora\typora-user-images\image-20260625211515702.png)

#### 视图 E: Messages 视图


![image-20260625211535745](C:\Users\86135\AppData\Roaming\Typora\typora-user-images\image-20260625211535745.png)


![image-20260625211547903](C:\Users\86135\AppData\Roaming\Typora\typora-user-images\image-20260625211547903.png)

#### 视图 F: Profile 视图

![image-20260625211613785](C:\Users\86135\AppData\Roaming\Typora\typora-user-images\image-20260625211613785.png)


#### 视图 G: Admin 视图
![image-20260625211755402](C:\Users\86135\AppData\Roaming\Typora\typora-user-images\image-20260625211755402.png)


### 2.2 帖子详情 (Index.vue 内嵌弹层)

<div style="max-width: 680px; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 28px 30px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); font-family: -apple-system, sans-serif;">
<div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #f1f5f9;"><span style="font-size: 14px; color: #3b82f6; font-weight: 500;">← 返回</span><div style="display: flex; gap: 16px; font-size: 14px; color: #64748b;"><span>🚩 举报</span><span>📤 分享</span></div></div>
<div style="margin-bottom: 12px;"><span style="background: #dbeafe; color: #1e40af; font-size: 12px; font-weight: 600; padding: 4px 14px; border-radius: 9999px;">📊 A股</span><span style="background: #f1f5f9; color: #475569; font-size: 12px; font-weight: 500; padding: 4px 14px; border-radius: 9999px; margin-left: 6px;">讨论</span></div>
<div style="font-size: 22px; font-weight: 700; color: #0f172a; line-height: 1.4; margin-bottom: 14px;">人工智能将如何重塑投资行业格局</div>
<div style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px;"><div style="width: 40px; height: 40px; background: #3b82f6; color: #ffffff; font-size: 16px; font-weight: 700; text-align: center; line-height: 40px; border-radius: 50%; flex-shrink: 0;">张</div><div><div style="font-size: 15px; font-weight: 600; color: #0f172a;">张三丰 <span style="background: #dbeafe; color: #1e40af; font-size: 11px; font-weight: 500; padding: 2px 8px; border-radius: 9999px; margin-left: 6px;">认证</span></div><div style="font-size: 13px; color: #94a3b8;">发布于 2026-06-25 · 👁️ 120</div></div></div>
<div style="font-size: 15px; color: #1e293b; line-height: 1.8; margin-bottom: 16px; padding: 16px 18px; background: #f8fafc; border-radius: 10px; border-left: 3px solid #3b82f6;">随着大模型技术的快速发展，AI在量化交易、风险评估、投研辅助等领域的应用正逐步深入。本篇文章将详细分析当前主流AI技术在投资领域的落地场景，并探讨未来的发展趋势与潜在风险...</div>
<div style="display: flex; gap: 24px; padding: 12px 0; border-top: 1px solid #f1f5f9; border-bottom: 1px solid #f1f5f9; margin-bottom: 20px;"><span style="font-size: 15px; color: #475569;">👍 <span style="font-weight: 600; color: #0f172a;">12</span></span><span style="font-size: 15px; color: #475569;">⭐ <span style="font-weight: 600; color: #0f172a;">5</span></span><span style="font-size: 15px; color: #475569;">💬 <span style="font-weight: 600; color: #0f172a;">8</span></span></div>
<div style="margin-top: 8px;"><div style="font-size: 16px; font-weight: 600; color: #0f172a; margin-bottom: 14px;">💬 全部评论 <span style="font-weight: 400; color: #94a3b8; font-size: 14px;">· 8 条</span></div>
<div style="display: flex; gap: 10px; margin-bottom: 18px;"><span style="flex: 1; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 9999px; padding: 10px 18px; font-size: 14px; color: #94a3b8;">写下你的评论...</span><span style="background: #1e293b; color: #ffffff; padding: 10px 22px; border-radius: 9999px; font-size: 14px; font-weight: 600;">发表</span></div>
<div style="padding: 14px 16px; background: #f8fafc; border-radius: 10px; border: 1px solid #f1f5f9; margin-bottom: 10px;"><div style="display: flex; align-items: flex-start; gap: 10px;"><div style="width: 32px; height: 32px; background: #10b981; color: #fff; font-size: 13px; font-weight: 700; text-align: center; line-height: 32px; border-radius: 50%; flex-shrink: 0;">A</div><div><div style="font-size: 14px; font-weight: 600; color: #0f172a;">用户A <span style="font-weight: 400; color: #94a3b8; font-size: 12px; margin-left: 8px;">2小时前</span></div><div style="font-size: 14px; color: #334155; margin-top: 2px;">非常认同，AI确实是未来投资的重要方向。</div></div></div></div>
<div style="padding: 14px 16px; background: #f8fafc; border-radius: 10px; border: 1px solid #f1f5f9; margin-bottom: 10px;"><div style="display: flex; align-items: flex-start; gap: 10px;"><div style="width: 32px; height: 32px; background: #8b5cf6; color: #fff; font-size: 13px; font-weight: 700; text-align: center; line-height: 32px; border-radius: 50%; flex-shrink: 0;">B</div><div><div style="font-size: 14px; font-weight: 600; color: #0f172a;">用户B <span style="font-weight: 400; color: #94a3b8; font-size: 12px; margin-left: 8px;">3小时前</span></div><div style="font-size: 14px; color: #334155; margin-top: 2px;">感谢分享，很受益！</div><div style="margin-top: 10px; padding-left: 12px; border-left: 2px solid #dbeafe;"><div style="display: flex; align-items: flex-start; gap: 10px;"><div style="width: 28px; height: 28px; background: #f59e0b; color: #fff; font-size: 12px; font-weight: 700; text-align: center; line-height: 28px; border-radius: 50%; flex-shrink: 0;">A</div><div><div style="font-size: 13px; font-weight: 600; color: #0f172a;">用户A <span style="font-weight: 400; color: #94a3b8; font-size: 12px; margin-left: 8px;">2小时前</span></div><div style="font-size: 13px; color: #334155; margin-top: 2px;">谢谢支持！欢迎多交流~</div></div></div></div></div></div></div>
<div style="padding: 14px 16px; background: #f8fafc; border-radius: 10px; border: 1px solid #f1f5f9;"><div style="display: flex; align-items: flex-start; gap: 10px;"><div style="width: 32px; height: 32px; background: #ef4444; color: #fff; font-size: 13px; font-weight: 700; text-align: center; line-height: 32px; border-radius: 50%; flex-shrink: 0;">C</div><div><div style="font-size: 14px; font-weight: 600; color: #0f172a;">用户C <span style="font-weight: 400; color: #94a3b8; font-size: 12px; margin-left: 8px;">5小时前</span></div><div style="font-size: 14px; color: #334155; margin-top: 2px;">文章写得很详细，期待下篇！</div></div></div></div>
</div>
</div>
### 2.3 登录页面 (Login.vue)

<div style="max-width: 880px; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 0; box-shadow: 0 4px 20px rgba(0,0,0,0.06); font-family: -apple-system, sans-serif; display: flex; overflow: hidden;">
<div style="flex: 1; background: linear-gradient(145deg, #0f172a 0%, #1e293b 100%); padding: 48px 36px; display: flex; flex-direction: column; justify-content: center; min-height: 500px;">
<div style="font-size: 48px; margin-bottom: 16px;">📈</div>
<div style="font-size: 28px; font-weight: 700; color: #ffffff; line-height: 1.3; margin-bottom: 12px;">投资未来</div>
<div style="font-size: 15px; color: #94a3b8; line-height: 1.6; max-width: 300px;">连接智慧与资本 · 发现价值与机遇</div>
<div style="margin-top: 30px; display: flex; gap: 10px; flex-wrap: wrap;"><span style="background: rgba(255,255,255,0.08); color: #cbd5e1; padding: 4px 14px; border-radius: 9999px; font-size: 12px;">📊 实时行情</span><span style="background: rgba(255,255,255,0.08); color: #cbd5e1; padding: 4px 14px; border-radius: 9999px; font-size: 12px;">🧠 AI 投研</span><span style="background: rgba(255,255,255,0.08); color: #cbd5e1; padding: 4px 14px; border-radius: 9999px; font-size: 12px;">👥 社区交流</span></div>
</div>
<div style="flex: 1; padding: 40px 36px 36px 36px; background: #ffffff;">
<div style="display: flex; gap: 0; margin-bottom: 24px; background: #f1f5f9; border-radius: 10px; padding: 4px;"><span style="flex: 1; text-align: center; padding: 8px 0; font-size: 15px; font-weight: 600; color: #ffffff; background: #1e293b; border-radius: 8px;">登录</span><span style="flex: 1; text-align: center; padding: 8px 0; font-size: 15px; font-weight: 500; color: #64748b;">注册</span></div>
<div style="margin-bottom: 16px;"><div style="font-size: 13px; font-weight: 500; color: #475569; margin-bottom: 4px;">账号</div><div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 10px 14px; font-size: 14px; color: #1e293b;">admin</div></div>
<div style="margin-bottom: 16px;"><div style="font-size: 13px; font-weight: 500; color: #475569; margin-bottom: 4px;">密码</div><div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 10px 14px; font-size: 14px; color: #1e293b;">········</div></div>
<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;"><span style="font-size: 13px; color: #3b82f6;">忘记密码？</span><span style="font-size: 13px; color: #94a3b8;">🔒 安全登录</span></div>
<div style="background: #1e293b; color: #ffffff; text-align: center; padding: 12px 0; border-radius: 8px; font-size: 15px; font-weight: 600; margin-bottom: 20px;">登 录</div>
<div style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px;"><hr style="flex: 1; border: 0; border-top: 1px solid #e2e8f0;"><span style="font-size: 12px; color: #94a3b8; white-space: nowrap;">第三方登录</span><hr style="flex: 1; border: 0; border-top: 1px solid #e2e8f0;"></div>
<div style="display: flex; justify-content: center; gap: 24px;"><span style="font-size: 28px;">💬</span><span style="font-size: 28px;">🐦</span><span style="font-size: 28px;">🐧</span></div>
<div style="margin-top: 16px; padding-top: 16px; border-top: 1px dashed #e2e8f0;"><div style="font-size: 12px; color: #94a3b8; text-align: center;">注册模式：用户名 · 手机号/邮箱 · 昵称 · 密码</div></div>
</div>
</div>

---

## 3. 交互设计说明

| 交互 | 说明 |
|------|------|
| 帖子点赞 | 点击切换点赞/取消点赞状态，实时更新计数 |
| 帖子收藏 | 点击切换收藏/取消收藏状态，实时更新计数 |
| 评论嵌套 | 评论可回复评论，最多递归 3 层 |
| 发帖 | 点击"发帖"区域展开完整表单 |
| 审核操作 | 管理员审核帖子，点击通过/驳回，驳回需填写理由 |
| 搜索 | 搜索框输入时自动防抖，支持回车提交 |
| 菜单切换 | 首页内部视图通过 Tab 切换，无页面跳转 |
| 移动端适配 | 底部导航栏替代侧边栏 |

---

## 4. 响应式说明

| 断点 | 布局 |
|------|------|
| <768px (移动端) | 单列布局，底部导航，隐藏侧边栏 |
| 768-1024px (平板) | 两列布局，侧边栏收缩 |
| >1024px (桌面) | 三列布局 (板块/内容/热门) |
