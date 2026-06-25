package com.stock.forum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.forum.auth.JwtService;
import com.stock.forum.common.ApiException;
import com.stock.forum.dto.ForumDtos;
import com.stock.forum.repository.ForumDataAccess;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ForumServiceUnitTest {
    private final ForumDataAccess data = mock(ForumDataAccess.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final ForumService service = new ForumService(data, jwtService, new ObjectMapper());

    @Test
    void registerRejectsMissingAccountIdentifier() {
        ForumDtos.RegisterRequest request = new ForumDtos.RegisterRequest();
        request.password = "case123456";

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("用户名、手机号或邮箱至少填写一项")
                .extracting("code")
                .isEqualTo(400);
    }

    @Test
    void registerRejectsShortPassword() {
        ForumDtos.RegisterRequest request = new ForumDtos.RegisterRequest();
        request.username = "short_password_user";
        request.password = "12345";

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(ApiException.class)
                .hasMessage("密码至少需要 6 位")
                .extracting("code")
                .isEqualTo(400);
    }

    @Test
    void createPostRejectsMissingBoard() {
        when(jwtService.parseUserId("token")).thenReturn(9L);
        when(data.queryOne("SELECT * FROM forum_boards WHERE id=?", 999L)).thenReturn(Optional.empty());
        ForumDtos.PostRequest request = new ForumDtos.PostRequest();
        request.boardId = 999L;
        request.title = "Missing board";
        request.content = "Content";

        assertThatThrownBy(() -> service.createPost("Bearer token", request))
                .isInstanceOf(ApiException.class)
                .hasMessage("板块不存在")
                .extracting("code")
                .isEqualTo(400);
    }

    @Test
    void followRejectsSelfBeforeWritingFollowRelation() {
        when(jwtService.parseUserId("token")).thenReturn(10L);

        assertThatThrownBy(() -> service.follow("Bearer token", 10L, true, false))
                .isInstanceOf(ApiException.class)
                .hasMessage("不能关注自己")
                .extracting("code")
                .isEqualTo(400);
        verify(data, never()).insert(eq("INSERT INTO forum_follows (follower_id, following_id, starred, created_at) VALUES (?, ?, ?, ?)"), anyLong(), anyLong(), eq(false), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void adminDashboardRejectsNormalUser() {
        when(jwtService.parseUserId("token")).thenReturn(11L);
        when(data.queryOne("SELECT * FROM forum_users WHERE id=?", 11L)).thenReturn(Optional.of(user(11L, "USER")));

        assertThatThrownBy(() -> service.adminDashboard("Bearer token"))
                .isInstanceOf(ApiException.class)
                .hasMessage("需要管理员权限")
                .extracting("code")
                .isEqualTo(401);
    }

    private Map<String, Object> user(Long id, String role) {
        Map<String, Object> user = new LinkedHashMap<String, Object>();
        user.put("id", id);
        user.put("role", role);
        user.put("status", "ACTIVE");
        user.put("nickName", "Unit User");
        return user;
    }
}
