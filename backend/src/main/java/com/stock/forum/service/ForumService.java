package com.stock.forum.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.forum.auth.JwtService;
import com.stock.forum.common.ApiException;
import com.stock.forum.common.PageResponse;
import com.stock.forum.dto.ForumDtos;
import com.stock.forum.repository.ForumDataAccess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ForumService {
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_PENDING = "PENDING_REVIEW";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MODERATOR = "MODERATOR";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ForumDataAccess data;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public ForumService(ForumDataAccess data, JwtService jwtService, ObjectMapper objectMapper) {
        this.data = data;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void seedDemoData() {
        if (data.count("SELECT COUNT(*) FROM forum_users") > 0) {
            return;
        }
        long adminId = createUserInternal("admin", "", "admin@forum.local", "forum-admin-2026", "运营管理员", ROLE_ADMIN, "PROFESSIONAL", true);
        long analystId = createUserInternal("analyst", "", "analyst@forum.local", "analyst123", "北向研究员", "PRO_USER", "PROFESSIONAL", true);
        createUserInternal("investor", "13800000000", "investor@forum.local", "investor123", "长期主义者", "USER", "BASIC", false);

        long aShare = boardId("a-share");
        long fund = boardId("fund");
        long macro = boardId("macro");
        createPostInternal(analystId, aShare, "LONG_ARTICLE", "市场缩量时如何观察行业轮动", "从成交额、估值分位和业绩预期三个维度观察轮动。", "缩量环境下，行业轮动通常会先体现在高股息、防御成长和政策催化主题之间。本文示例用成交额占比、估值分位和盈利修正方向搭建观察框架，不构成任何投资建议。", "[\"000300\"]", STATUS_PUBLISHED, true);
        createPostInternal(analystId, fund, "NORMAL", "指数基金定投需要看哪些指标", "费率、跟踪误差、规模和流动性是四个基础指标。", "指数基金适合用长期视角评估，除历史收益外，更应关注跟踪误差、规模稳定性、申赎效率和底层指数编制逻辑。", "[\"510300\"]", STATUS_PUBLISHED, false);
        createPostInternal(adminId, macro, "SHORT", "盘前观察：关注政策预期和海外利率", "今日热榜种子内容。", "今日市场可重点关注政策预期、海外利率走势以及北向资金变化。讨论请避免承诺收益和直接荐股。", "[]", STATUS_PUBLISHED, true);
    }

    @Transactional
    public ForumDtos.AuthResponse register(ForumDtos.RegisterRequest request) {
        String username = clean(request.username);
        String phone = clean(request.phone);
        String email = clean(request.email);
        String password = clean(request.password);
        String nickName = clean(request.nickName);
        if (isBlank(username) && isBlank(phone) && isBlank(email)) {
            throw ApiException.badRequest("用户名、手机号或邮箱至少填写一项");
        }
        if (password.length() < 6) {
            throw ApiException.badRequest("密码至少需要 6 位");
        }
        ensureUnique("username", username);
        ensureUnique("phone", phone);
        ensureUnique("email", email);
        String role = data.count("SELECT COUNT(*) FROM forum_users") == 0 ? ROLE_ADMIN : "USER";
        long userId = createUserInternal(username, phone, email, password, isBlank(nickName) ? defaultNick(username, phone, email) : nickName, role, "BASIC", false);
        return authResponse(userById(userId));
    }

    public ForumDtos.AuthResponse login(ForumDtos.LoginRequest request) {
        String account = clean(request.account);
        String password = clean(request.password);
        if (isBlank(account) || isBlank(password)) {
            throw ApiException.badRequest("账号和密码不能为空");
        }
        Map<String, Object> user = data.queryOne("SELECT * FROM forum_users WHERE username=? OR phone=? OR email=? LIMIT 1", account, account, account)
                .orElseThrow(() -> ApiException.unauthorized("账号或密码错误"));
        if (!verifyPassword(password, string(user.get("passwordSalt")), string(user.get("passwordHash")))) {
            throw ApiException.unauthorized("账号或密码错误");
        }
        String status = string(user.get("status"));
        if ("BANNED".equals(status)) {
            throw ApiException.unauthorized("账号已被封禁");
        }
        return authResponse(user);
    }

    public Map<String, Object> me(String authorization) {
        Long userId = requireUserId(authorization);
        Map<String, Object> user = cleanUser(userById(userId));
        user.put("postCount", data.count("SELECT COUNT(*) FROM forum_posts WHERE author_id=?", userId));
        user.put("followerCount", data.count("SELECT COUNT(*) FROM forum_follows WHERE following_id=?", userId));
        user.put("followingCount", data.count("SELECT COUNT(*) FROM forum_follows WHERE follower_id=?", userId));
        return user;
    }

    @Transactional
    public Map<String, Object> updateProfile(String authorization, ForumDtos.ProfileRequest request) {
        Long userId = requireUserId(authorization);
        data.update("UPDATE forum_users SET nick_name=?, avatar_url=?, bio=?, experience_tags=?, markets=?, risk_preference=?, privacy_profile=?, updated_at=? WHERE id=?",
                fallback(request.nickName, "投资者"),
                fallback(request.avatarUrl, ""),
                fallback(request.bio, ""),
                toJson(request.experienceTags),
                toJson(request.markets),
                fallback(request.riskPreference, "BALANCED"),
                fallback(request.privacyProfile, "PUBLIC"),
                data.now(),
                userId);
        return cleanUser(userById(userId));
    }

    public List<Map<String, Object>> listBoards(boolean includeDisabled) {
        if (includeDisabled) {
            return data.query("SELECT * FROM forum_boards ORDER BY sort_order ASC, id ASC");
        }
        return data.query("SELECT * FROM forum_boards WHERE enabled=TRUE ORDER BY sort_order ASC, id ASC");
    }

    @Transactional
    public Map<String, Object> createBoard(String authorization, ForumDtos.BoardRequest request) {
        requireAdmin(authorization);
        Timestamp now = data.now();
        long id = data.insert("INSERT INTO forum_boards (name, slug, category, description, market, sort_order, enabled, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                required(request.name, "板块名称不能为空"),
                required(request.slug, "板块标识不能为空"),
                fallback(request.category, "主题专区"),
                fallback(request.description, ""),
                fallback(request.market, ""),
                request.sortOrder == null ? 100 : request.sortOrder,
                request.enabled == null ? Boolean.TRUE : request.enabled,
                now,
                now);
        return boardById(id);
    }

    @Transactional
    public Map<String, Object> updateBoard(String authorization, Long boardId, ForumDtos.BoardRequest request) {
        requireAdmin(authorization);
        Map<String, Object> board = boardById(boardId);
        data.update("UPDATE forum_boards SET name=?, slug=?, category=?, description=?, market=?, sort_order=?, enabled=?, updated_at=? WHERE id=?",
                fallback(request.name, string(board.get("name"))),
                fallback(request.slug, string(board.get("slug"))),
                fallback(request.category, string(board.get("category"))),
                fallback(request.description, string(board.get("description"))),
                fallback(request.market, string(board.get("market"))),
                request.sortOrder == null ? intValue(board.get("sortOrder"), 100) : request.sortOrder,
                request.enabled == null ? bool(board.get("enabled")) : request.enabled,
                data.now(),
                boardId);
        return boardById(boardId);
    }

    @Transactional
    public Map<String, Object> deleteBoard(String authorization, Long boardId) {
        requireAdmin(authorization);
        data.update("UPDATE forum_boards SET enabled=FALSE, updated_at=? WHERE id=?", data.now(), boardId);
        return success();
    }

    @Transactional
    public Map<String, Object> createPost(String authorization, ForumDtos.PostRequest request) {
        Long userId = requireUserId(authorization);
        boardById(request.boardId);
        String title = required(request.title, "标题不能为空");
        String content = required(request.content, "内容不能为空");
        String sensitive = findSensitiveWord(title + " " + content);
        String status = STATUS_PENDING;
        String reason = sensitive == null ? "" : "命中敏感词：" + sensitive;
        Timestamp now = data.now();
        long id = data.insert("INSERT INTO forum_posts (author_id, board_id, type, title, summary, content, images, attachments, stock_codes, status, review_reason, digest, created_at, updated_at, published_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE, ?, ?, NULL)",
                userId,
                request.boardId,
                fallback(request.type, "NORMAL"),
                title,
                fallback(request.summary, ""),
                content,
                toJson(request.images),
                toJson(request.attachments),
                toJson(request.stockCodes),
                status,
                reason,
                now,
                now);
        data.update("UPDATE forum_users SET post_count=post_count+1, points=points+5, influence=influence+1, updated_at=? WHERE id=?", now, userId);
        return postById(id);
    }

    public PageResponse<Map<String, Object>> listPublishedPosts(Long boardId, String keyword, int page, int size) {
        List<Object> args = new ArrayList<Object>();
        StringBuilder where = new StringBuilder(" WHERE p.status=?");
        args.add(STATUS_PUBLISHED);
        if (boardId != null) {
            where.append(" AND p.board_id=?");
            args.add(boardId);
        }
        appendKeyword(where, args, keyword);
        return queryPosts(where.toString(), args, page, size);
    }

    public PageResponse<Map<String, Object>> listMyPosts(String authorization, int page, int size) {
        Long userId = requireUserId(authorization);
        List<Object> args = data.args(userId);
        return queryPosts(" WHERE p.author_id=?", args, page, size);
    }

    public PageResponse<Map<String, Object>> followingFeed(String authorization, int page, int size) {
        Long userId = requireUserId(authorization);
        List<Object> args = data.args(userId, STATUS_PUBLISHED);
        return queryPosts(" INNER JOIN forum_follows f ON f.following_id=p.author_id AND f.follower_id=? WHERE p.status=?", args, page, size);
    }

    @Transactional
    public Map<String, Object> getPost(Long postId, String authorization) {
        Map<String, Object> post = postById(postId);
        Long userId = resolveUserId(authorization, false);
        if (!STATUS_PUBLISHED.equals(string(post.get("status")))) {
            if (userId == null || (!userId.equals(longValue(post.get("authorId"))) && !isAdmin(userById(userId)))) {
                throw ApiException.unauthorized("无权查看该内容");
            }
        }
        data.update("UPDATE forum_posts SET view_count=view_count+1 WHERE id=?", postId);
        post.put("comments", listComments(postId));
        if (userId != null) {
            post.put("viewerInteractions", viewerInteractions(userId, "POST", postId));
        }
        return post;
    }

    public List<Map<String, Object>> listComments(Long postId) {
        return data.query("SELECT c.*, u.nick_name AS author_name, u.avatar_url AS author_avatar, u.professional_badge AS author_badge FROM forum_comments c INNER JOIN forum_users u ON u.id=c.user_id WHERE c.post_id=? AND c.status='PUBLISHED' ORDER BY COALESCE(c.parent_id, c.id) ASC, c.created_at ASC", postId);
    }

    @Transactional
    public Map<String, Object> createComment(String authorization, Long postId, ForumDtos.CommentRequest request) {
        Long userId = requireUserId(authorization);
        Map<String, Object> post = postById(postId);
        if (!STATUS_PUBLISHED.equals(string(post.get("status")))) {
            throw ApiException.badRequest("帖子尚未发布，不能评论");
        }
        String content = required(request.content, "评论不能为空");
        String sensitive = findSensitiveWord(content);
        if (sensitive != null) {
            throw ApiException.badRequest("评论命中敏感词：" + sensitive);
        }
        Timestamp now = data.now();
        long id = data.insert("INSERT INTO forum_comments (post_id, user_id, parent_id, reply_to_id, content, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, 'PUBLISHED', ?, ?)",
                postId,
                userId,
                request.parentId,
                request.replyToId,
                content,
                now,
                now);
        data.update("UPDATE forum_posts SET comment_count=comment_count+1, updated_at=? WHERE id=?", now, postId);
        return data.queryOne("SELECT c.*, u.nick_name AS author_name, u.avatar_url AS author_avatar FROM forum_comments c INNER JOIN forum_users u ON u.id=c.user_id WHERE c.id=?", id).get();
    }

    @Transactional
    public Map<String, Object> interact(String authorization, Long postId, ForumDtos.InteractionRequest request) {
        Long userId = requireUserId(authorization);
        postById(postId);
        String action = fallback(request.action, "LIKE").toUpperCase();
        boolean active = request.active == null || request.active;
        String counter = interactionCounter(action);
        Optional<Map<String, Object>> existing = data.queryOne("SELECT * FROM forum_interactions WHERE user_id=? AND target_type='POST' AND target_id=? AND action=?", userId, postId, action);
        boolean wasActive = existing.isPresent() && bool(existing.get().get("active"));
        if (existing.isPresent()) {
            data.update("UPDATE forum_interactions SET active=?, updated_at=? WHERE id=?", active, data.now(), existing.get().get("id"));
        } else {
            data.insert("INSERT INTO forum_interactions (user_id, target_type, target_id, action, active, created_at, updated_at) VALUES (?, 'POST', ?, ?, ?, ?, ?)", userId, postId, action, active, data.now(), data.now());
        }
        if (counter != null && wasActive != active) {
            int delta = active ? 1 : -1;
            data.update("UPDATE forum_posts SET " + counter + "=" + counter + "+?, updated_at=? WHERE id=?", delta, data.now(), postId);
        }
        return postById(postId);
    }

    @Transactional
    public Map<String, Object> report(String authorization, ForumDtos.ReportRequest request) {
        Long userId = requireUserId(authorization);
        String targetType = fallback(request.targetType, "POST").toUpperCase();
        Long targetId = request.targetId;
        if (targetId == null) {
            throw ApiException.badRequest("举报目标不能为空");
        }
        long id = data.insert("INSERT INTO forum_reports (reporter_id, target_type, target_id, reason, detail, status, created_at) VALUES (?, ?, ?, ?, ?, 'OPEN', ?)",
                userId,
                targetType,
                targetId,
                required(request.reason, "举报原因不能为空"),
                fallback(request.detail, ""),
                data.now());
        return data.queryOne("SELECT * FROM forum_reports WHERE id=?", id).get();
    }

    @Transactional
    public Map<String, Object> follow(String authorization, Long targetUserId, boolean active, boolean starred) {
        Long userId = requireUserId(authorization);
        if (userId.equals(targetUserId)) {
            throw ApiException.badRequest("不能关注自己");
        }
        userById(targetUserId);
        Optional<Map<String, Object>> existing = data.queryOne("SELECT * FROM forum_follows WHERE follower_id=? AND following_id=?", userId, targetUserId);
        if (active) {
            if (existing.isPresent()) {
                data.update("UPDATE forum_follows SET starred=? WHERE id=?", starred, existing.get().get("id"));
            } else {
                data.insert("INSERT INTO forum_follows (follower_id, following_id, starred, created_at) VALUES (?, ?, ?, ?)", userId, targetUserId, starred, data.now());
            }
        } else if (existing.isPresent()) {
            data.update("DELETE FROM forum_follows WHERE id=?", existing.get().get("id"));
        }
        return success();
    }

    public Map<String, Object> search(String keyword, int page, int size) {
        String key = "%" + fallback(keyword, "") + "%";
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("posts", listPublishedPosts(null, keyword, page, size));
        result.put("users", data.query("SELECT id, nick_name, avatar_url, bio, role, verification_level, professional_badge, influence FROM forum_users WHERE status='ACTIVE' AND (nick_name LIKE ? OR username LIKE ?) ORDER BY influence DESC LIMIT 8", key, key));
        result.put("symbols", data.query("SELECT code, name, market, aliases FROM forum_stock_symbols WHERE code LIKE ? OR name LIKE ? OR aliases LIKE ? ORDER BY market ASC, code ASC LIMIT 10", key, key, key));
        result.put("engine", "jdbc-fallback");
        return result;
    }

    public List<Map<String, Object>> suggest(String keyword) {
        String key = "%" + fallback(keyword, "") + "%";
        return data.query("SELECT code, name, market, aliases FROM forum_stock_symbols WHERE code LIKE ? OR name LIKE ? OR aliases LIKE ? ORDER BY market ASC, code ASC LIMIT 10", key, key, key);
    }

    @Transactional
    public Map<String, Object> submitVerification(String authorization, ForumDtos.VerificationRequest request) {
        Long userId = requireUserId(authorization);
        String type = fallback(request.type, "BASIC").toUpperCase();
        Timestamp now = data.now();
        String status = "BASIC".equals(type) ? "APPROVED" : "PENDING";
        data.insert("INSERT INTO forum_verifications (user_id, type, real_name, id_number, provider, external_request_id, materials, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                userId,
                type,
                fallback(request.realName, ""),
                fallback(request.idNumber, ""),
                fallback(request.provider, ""),
                fallback(request.externalRequestId, ""),
                toJson(request.materials),
                status,
                now,
                now);
        if ("BASIC".equals(type)) {
            data.update("UPDATE forum_users SET verification_level='BASIC', updated_at=? WHERE id=?", now, userId);
        }
        return me(authorization);
    }

    @Transactional
    public Map<String, Object> completeRiskAssessment(String authorization, ForumDtos.RiskAssessmentRequest request) {
        Long userId = requireUserId(authorization);
        int score = request.score == null ? 0 : request.score;
        String riskLevel = fallback(request.riskLevel, score >= 80 ? "AGGRESSIVE" : score >= 50 ? "BALANCED" : "CONSERVATIVE");
        data.insert("INSERT INTO forum_risk_assessments (user_id, score, risk_level, answers, status, created_at) VALUES (?, ?, ?, ?, 'COMPLETED', ?)",
                userId,
                score,
                riskLevel,
                toJson(request.answers),
                data.now());
        data.update("UPDATE forum_users SET suitability_status='COMPLETED', risk_preference=?, updated_at=? WHERE id=?", riskLevel, data.now(), userId);
        return me(authorization);
    }

    @Transactional
    public Map<String, Object> createGroup(String authorization, ForumDtos.GroupRequest request) {
        Long userId = requireUserId(authorization);
        Timestamp now = data.now();
        long id = data.insert("INSERT INTO forum_groups (owner_id, name, description, visibility, join_policy, member_count, created_at, updated_at) VALUES (?, ?, ?, ?, ?, 1, ?, ?)",
                userId,
                required(request.name, "群组名称不能为空"),
                fallback(request.description, ""),
                fallback(request.visibility, "PUBLIC").toUpperCase(),
                fallback(request.joinPolicy, "OPEN").toUpperCase(),
                now,
                now);
        data.insert("INSERT INTO forum_group_members (group_id, user_id, role, status, created_at) VALUES (?, ?, 'OWNER', 'ACTIVE', ?)", id, userId, now);
        return data.queryOne("SELECT g.*, u.nick_name AS owner_name FROM forum_groups g INNER JOIN forum_users u ON u.id=g.owner_id WHERE g.id=?", id).get();
    }

    public List<Map<String, Object>> listGroups() {
        return data.query("SELECT g.*, u.nick_name AS owner_name FROM forum_groups g INNER JOIN forum_users u ON u.id=g.owner_id WHERE g.visibility='PUBLIC' ORDER BY g.created_at DESC LIMIT 50");
    }

    @Transactional
    public Map<String, Object> joinGroup(String authorization, Long groupId) {
        Long userId = requireUserId(authorization);
        Optional<Map<String, Object>> existing = data.queryOne("SELECT * FROM forum_group_members WHERE group_id=? AND user_id=?", groupId, userId);
        if (!existing.isPresent()) {
            data.insert("INSERT INTO forum_group_members (group_id, user_id, role, status, created_at) VALUES (?, ?, 'MEMBER', 'ACTIVE', ?)", groupId, userId, data.now());
            data.update("UPDATE forum_groups SET member_count=member_count+1, updated_at=? WHERE id=?", data.now(), groupId);
        }
        return success();
    }

    @Transactional
    public Map<String, Object> sendMessage(String authorization, ForumDtos.MessageRequest request) {
        Long userId = requireUserId(authorization);
        userById(request.receiverId);
        long id = data.insert("INSERT INTO forum_messages (sender_id, receiver_id, content, image_url, created_at) VALUES (?, ?, ?, ?, ?)",
                userId,
                request.receiverId,
                required(request.content, "私信内容不能为空"),
                fallback(request.imageUrl, ""),
                data.now());
        return data.queryOne("SELECT * FROM forum_messages WHERE id=?", id).get();
    }

    public List<Map<String, Object>> listMessages(String authorization, Long peerId) {
        Long userId = requireUserId(authorization);
        return data.query("SELECT m.*, s.nick_name AS sender_name, r.nick_name AS receiver_name FROM forum_messages m INNER JOIN forum_users s ON s.id=m.sender_id INNER JOIN forum_users r ON r.id=m.receiver_id WHERE (m.sender_id=? AND m.receiver_id=?) OR (m.sender_id=? AND m.receiver_id=?) ORDER BY m.created_at ASC LIMIT 100", userId, peerId, peerId, userId);
    }

    public Map<String, Object> adminDashboard(String authorization) {
        requireAdmin(authorization);
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("users", data.count("SELECT COUNT(*) FROM forum_users"));
        result.put("posts", data.count("SELECT COUNT(*) FROM forum_posts"));
        result.put("pendingPosts", data.count("SELECT COUNT(*) FROM forum_posts WHERE status=?", STATUS_PENDING));
        result.put("openReports", data.count("SELECT COUNT(*) FROM forum_reports WHERE status='OPEN'"));
        result.put("publishedPosts", data.count("SELECT COUNT(*) FROM forum_posts WHERE status=?", STATUS_PUBLISHED));
        result.put("boards", data.count("SELECT COUNT(*) FROM forum_boards WHERE enabled=TRUE"));
        result.put("topBoards", data.query("SELECT b.id, b.name, COUNT(p.id) AS post_count FROM forum_boards b LEFT JOIN forum_posts p ON p.board_id=b.id AND p.status='PUBLISHED' GROUP BY b.id, b.name ORDER BY post_count DESC, b.sort_order ASC LIMIT 8"));
        result.put("hotPosts", data.query(postSelect() + " WHERE p.status='PUBLISHED' ORDER BY (p.like_count*3+p.comment_count*4+p.view_count) DESC, p.published_at DESC LIMIT 8"));
        return result;
    }

    public PageResponse<Map<String, Object>> adminReviewPosts(String authorization, String status, int page, int size) {
        requireAdmin(authorization);
        List<Object> args = data.args(fallback(status, STATUS_PENDING));
        return queryPosts(" WHERE p.status=?", args, page, size);
    }

    @Transactional
    public Map<String, Object> reviewPost(String authorization, Long postId, ForumDtos.ReviewRequest request) {
        Long adminId = requireAdmin(authorization);
        String decision = fallback(request.decision, "APPROVE").toUpperCase();
        String nextStatus = "REJECT".equals(decision) || STATUS_REJECTED.equals(decision) ? STATUS_REJECTED : STATUS_PUBLISHED;
        Timestamp now = data.now();
        data.update("UPDATE forum_posts SET status=?, review_reason=?, published_at=?, updated_at=? WHERE id=?",
                nextStatus,
                fallback(request.reason, ""),
                STATUS_PUBLISHED.equals(nextStatus) ? now : null,
                now,
                postId);
        data.insert("INSERT INTO forum_audit_logs (operator_id, action, target_type, target_id, detail, created_at) VALUES (?, 'REVIEW_POST', 'POST', ?, ?, ?)", adminId, postId, nextStatus, now);
        return postById(postId);
    }

    public PageResponse<Map<String, Object>> listReports(String authorization, String status, int page, int size) {
        requireAdmin(authorization);
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        String where = isBlank(status) ? "" : " WHERE r.status=?";
        Object[] args = isBlank(status) ? new Object[]{} : new Object[]{status};
        long total = data.count("SELECT COUNT(*) FROM forum_reports r" + where, args);
        List<Object> listArgs = new ArrayList<Object>();
        Collections.addAll(listArgs, args);
        listArgs.add(safeSize);
        listArgs.add((safePage - 1) * safeSize);
        List<Map<String, Object>> records = data.query("SELECT r.*, u.nick_name AS reporter_name FROM forum_reports r INNER JOIN forum_users u ON u.id=r.reporter_id" + where + " ORDER BY r.created_at DESC LIMIT ? OFFSET ?", listArgs.toArray());
        return PageResponse.of(records, total, safePage, safeSize);
    }

    @Transactional
    public Map<String, Object> resolveReport(String authorization, Long reportId, ForumDtos.ReviewRequest request) {
        Long adminId = requireAdmin(authorization);
        data.update("UPDATE forum_reports SET status='RESOLVED', handled_by=?, handled_at=? WHERE id=?", adminId, data.now(), reportId);
        data.insert("INSERT INTO forum_audit_logs (operator_id, action, target_type, target_id, detail, created_at) VALUES (?, 'RESOLVE_REPORT', 'REPORT', ?, ?, ?)", adminId, reportId, fallback(request.reason, ""), data.now());
        return success();
    }

    public PageResponse<Map<String, Object>> listUsers(String authorization, String keyword, int page, int size) {
        requireAdmin(authorization);
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        List<Object> args = new ArrayList<Object>();
        StringBuilder where = new StringBuilder();
        if (!isBlank(keyword)) {
            where.append(" WHERE username LIKE ? OR nick_name LIKE ? OR email LIKE ? OR phone LIKE ?");
            String key = "%" + keyword + "%";
            args.add(key);
            args.add(key);
            args.add(key);
            args.add(key);
        }
        long total = data.count("SELECT COUNT(*) FROM forum_users" + where, args.toArray());
        args.add(safeSize);
        args.add((safePage - 1) * safeSize);
        List<Map<String, Object>> records = data.query("SELECT id, username, phone, email, nick_name, avatar_url, role, verification_level, professional_badge, status, points, user_level, post_count, influence, created_at FROM forum_users" + where + " ORDER BY created_at DESC LIMIT ? OFFSET ?", args.toArray());
        return PageResponse.of(records, total, safePage, safeSize);
    }

    @Transactional
    public Map<String, Object> applyViolation(String authorization, Long userId, ForumDtos.ViolationRequest request) {
        Long adminId = requireAdmin(authorization);
        String action = fallback(request.action, "WARN").toUpperCase();
        if ("BAN".equals(action)) {
            data.update("UPDATE forum_users SET status='BANNED', updated_at=? WHERE id=?", data.now(), userId);
        } else if ("MUTE".equals(action)) {
            data.update("UPDATE forum_users SET status='MUTED', updated_at=? WHERE id=?", data.now(), userId);
        }
        data.insert("INSERT INTO forum_notifications (user_id, type, title, content, created_at) VALUES (?, 'VIOLATION', ?, ?, ?)",
                userId,
                "社区违规处理：" + action,
                fallback(request.reason, "请遵守社区合规规范"),
                data.now());
        data.insert("INSERT INTO forum_audit_logs (operator_id, action, target_type, target_id, detail, created_at) VALUES (?, 'USER_VIOLATION', 'USER', ?, ?, ?)", adminId, userId, action + ":" + fallback(request.reason, ""), data.now());
        return success();
    }

    public List<Map<String, Object>> listSensitiveWords(String authorization) {
        requireAdmin(authorization);
        return data.query("SELECT * FROM forum_sensitive_words ORDER BY created_at DESC");
    }

    @Transactional
    public Map<String, Object> createSensitiveWord(String authorization, ForumDtos.SensitiveWordRequest request) {
        requireAdmin(authorization);
        long id = data.insert("INSERT INTO forum_sensitive_words (word, category, enabled, created_at) VALUES (?, ?, ?, ?)",
                required(request.word, "敏感词不能为空"),
                fallback(request.category, "合规风险"),
                request.enabled == null ? Boolean.TRUE : request.enabled,
                data.now());
        return data.queryOne("SELECT * FROM forum_sensitive_words WHERE id=?", id).get();
    }

    @Transactional
    public Map<String, Object> deleteSensitiveWord(String authorization, Long id) {
        requireAdmin(authorization);
        data.update("DELETE FROM forum_sensitive_words WHERE id=?", id);
        return success();
    }

    private PageResponse<Map<String, Object>> queryPosts(String whereClause, List<Object> args, int page, int size) {
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        String joinAwareWhere = whereClause == null ? "" : whereClause;
        String countSql;
        if (joinAwareWhere.trim().startsWith("INNER JOIN")) {
            countSql = "SELECT COUNT(*) FROM forum_posts p " + joinAwareWhere;
        } else {
            countSql = "SELECT COUNT(*) FROM forum_posts p" + joinAwareWhere;
        }
        long total = data.count(countSql, args.toArray());
        List<Object> listArgs = new ArrayList<Object>(args);
        listArgs.add(safeSize);
        listArgs.add((safePage - 1) * safeSize);
        String sql = postSelect() + joinAwareWhere + " ORDER BY p.published_at DESC, p.created_at DESC LIMIT ? OFFSET ?";
        return PageResponse.of(data.query(sql, listArgs.toArray()), total, safePage, safeSize);
    }

    private String postSelect() {
        return "SELECT p.*, u.nick_name AS author_name, u.avatar_url AS author_avatar, u.role AS author_role, u.professional_badge AS author_badge, b.name AS board_name, b.category AS board_category FROM forum_posts p INNER JOIN forum_users u ON u.id=p.author_id INNER JOIN forum_boards b ON b.id=p.board_id";
    }

    private void appendKeyword(StringBuilder where, List<Object> args, String keyword) {
        if (!isBlank(keyword)) {
            where.append(" AND (p.title LIKE ? OR p.content LIKE ? OR p.stock_codes LIKE ?)");
            String key = "%" + keyword + "%";
            args.add(key);
            args.add(key);
            args.add(key);
        }
    }

    private Map<String, Object> postById(Long id) {
        if (id == null) {
            throw ApiException.badRequest("帖子不存在");
        }
        return data.queryOne(postSelect() + " WHERE p.id=?", id)
                .orElseThrow(() -> ApiException.badRequest("帖子不存在"));
    }

    private Map<String, Object> boardById(Long id) {
        if (id == null) {
            throw ApiException.badRequest("板块不存在");
        }
        return data.queryOne("SELECT * FROM forum_boards WHERE id=?", id)
                .orElseThrow(() -> ApiException.badRequest("板块不存在"));
    }

    private long boardId(String slug) {
        return longValue(data.queryOne("SELECT id FROM forum_boards WHERE slug=?", slug).get().get("id"));
    }

    private Map<String, Object> userById(Long id) {
        if (id == null) {
            throw ApiException.badRequest("用户不存在");
        }
        return data.queryOne("SELECT * FROM forum_users WHERE id=?", id)
                .orElseThrow(() -> ApiException.badRequest("用户不存在"));
    }

    private Map<String, Object> cleanUser(Map<String, Object> user) {
        Map<String, Object> copy = new LinkedHashMap<String, Object>(user);
        copy.remove("passwordHash");
        copy.remove("passwordSalt");
        return copy;
    }

    private ForumDtos.AuthResponse authResponse(Map<String, Object> user) {
        ForumDtos.AuthResponse response = new ForumDtos.AuthResponse();
        Long userId = longValue(user.get("id"));
        response.userId = String.valueOf(userId);
        response.nickName = string(user.get("nickName"));
        response.avatarUrl = string(user.get("avatarUrl"));
        response.token = jwtService.createToken(userId);
        response.role = string(user.get("role"));
        response.verificationLevel = string(user.get("verificationLevel"));
        response.professionalBadge = bool(user.get("professionalBadge"));
        return response;
    }

    private Long requireAdmin(String authorization) {
        Long userId = requireUserId(authorization);
        if (!isAdmin(userById(userId))) {
            throw ApiException.unauthorized("需要管理员权限");
        }
        return userId;
    }

    private boolean isAdmin(Map<String, Object> user) {
        String role = string(user.get("role"));
        return ROLE_ADMIN.equals(role) || ROLE_MODERATOR.equals(role);
    }

    private Long requireUserId(String authorization) {
        return resolveUserId(authorization, true);
    }

    private Long resolveUserId(String authorization, boolean required) {
        String token = extractToken(authorization);
        if (isBlank(token)) {
            if (required) {
                throw ApiException.unauthorized("请先登录");
            }
            return null;
        }
        try {
            return jwtService.parseUserId(token);
        } catch (Exception ex) {
            if (required) {
                throw ex;
            }
            return null;
        }
    }

    private String extractToken(String authorization) {
        if (authorization == null) {
            return "";
        }
        String value = authorization.trim();
        if (value.toLowerCase().startsWith("bearer ")) {
            return value.substring(7).trim();
        }
        return value;
    }

    private long createUserInternal(String username, String phone, String email, String password, String nickName, String role, String verificationLevel, boolean professionalBadge) {
        String salt = newSalt();
        String hash = hashPassword(password, salt);
        Timestamp now = data.now();
        return data.insert("INSERT INTO forum_users (username, phone, email, password_hash, password_salt, nick_name, avatar_url, bio, experience_tags, markets, risk_preference, role, verification_level, professional_badge, suitability_status, privacy_profile, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, '', '', '[]', '[]', 'BALANCED', ?, ?, ?, 'NOT_STARTED', 'PUBLIC', 'ACTIVE', ?, ?)",
                emptyToNull(username),
                emptyToNull(phone),
                emptyToNull(email),
                hash,
                salt,
                nickName,
                role,
                verificationLevel,
                professionalBadge,
                now,
                now);
    }

    private void createPostInternal(long userId, long boardId, String type, String title, String summary, String content, String stockCodes, String status, boolean digest) {
        Timestamp now = data.now();
        data.insert("INSERT INTO forum_posts (author_id, board_id, type, title, summary, content, images, attachments, stock_codes, status, review_reason, digest, like_count, favorite_count, share_count, comment_count, view_count, created_at, updated_at, published_at) VALUES (?, ?, ?, ?, ?, ?, '[]', '[]', ?, ?, '', ?, 0, 0, 0, 0, 0, ?, ?, ?)",
                userId,
                boardId,
                type,
                title,
                summary,
                content,
                stockCodes,
                status,
                digest,
                now,
                now,
                STATUS_PUBLISHED.equals(status) ? now : null);
    }

    private Map<String, Object> viewerInteractions(Long userId, String targetType, Long targetId) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        List<Map<String, Object>> rows = data.query("SELECT action, active FROM forum_interactions WHERE user_id=? AND target_type=? AND target_id=?", userId, targetType, targetId);
        for (Map<String, Object> row : rows) {
            result.put(string(row.get("action")).toLowerCase(), bool(row.get("active")));
        }
        return result;
    }

    private String findSensitiveWord(String content) {
        if (isBlank(content)) {
            return null;
        }
        String lower = content.toLowerCase();
        List<Map<String, Object>> words = data.query("SELECT word FROM forum_sensitive_words WHERE enabled=TRUE");
        for (Map<String, Object> row : words) {
            String word = string(row.get("word"));
            if (!isBlank(word) && lower.contains(word.toLowerCase())) {
                return word;
            }
        }
        return null;
    }

    private String interactionCounter(String action) {
        if ("LIKE".equals(action)) {
            return "like_count";
        }
        if ("FAVORITE".equals(action)) {
            return "favorite_count";
        }
        if ("SHARE".equals(action)) {
            return "share_count";
        }
        return null;
    }

    private void ensureUnique(String column, String value) {
        if (isBlank(value)) {
            return;
        }
        if (!"username".equals(column) && !"phone".equals(column) && !"email".equals(column)) {
            throw ApiException.badRequest("非法字段");
        }
        if (data.count("SELECT COUNT(*) FROM forum_users WHERE " + column + "=?", value) > 0) {
            throw ApiException.badRequest("账号信息已被使用");
        }
    }

    private String hashPassword(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), 12000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (Exception ex) {
            throw ApiException.serverError("密码处理失败");
        }
    }

    private boolean verifyPassword(String password, String salt, String expectedHash) {
        String actual = hashPassword(password, salt);
        int diff = actual.length() ^ expectedHash.length();
        int max = Math.max(actual.length(), expectedHash.length());
        for (int i = 0; i < max; i += 1) {
            char a = i < actual.length() ? actual.charAt(i) : 0;
            char b = i < expectedHash.length() ? expectedHash.charAt(i) : 0;
            diff |= a ^ b;
        }
        return diff == 0;
    }

    private String newSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Collections.emptyList() : value);
        } catch (Exception ex) {
            throw ApiException.badRequest("数据格式错误");
        }
    }

    @SuppressWarnings("unused")
    private List<String> fromJsonList(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> success() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("success", true);
        return result;
    }

    private String required(String value, String message) {
        String cleaned = clean(value);
        if (isBlank(cleaned)) {
            throw ApiException.badRequest(message);
        }
        return cleaned;
    }

    private String fallback(String value, String fallback) {
        String cleaned = clean(value);
        return isBlank(cleaned) ? fallback : cleaned;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String emptyToNull(String value) {
        return isBlank(value) ? null : value;
    }

    private String defaultNick(String username, String phone, String email) {
        if (!isBlank(username)) {
            return username;
        }
        if (!isBlank(phone)) {
            return "用户" + phone.substring(Math.max(0, phone.length() - 4));
        }
        return email;
    }

    private String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean bool(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return Boolean.parseBoolean(string(value));
    }

    private long longValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(string(value));
    }

    private int intValue(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(string(value));
        } catch (Exception ex) {
            return fallback;
        }
    }

    private int safePage(int page) {
        return page <= 0 ? 1 : page;
    }

    private int safeSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
