package com.stock.forum.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ForumApiExceptionTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rejectsRegisterWithoutAnyAccountIdentifier() throws Exception {
        JsonNode error = apiRoot(mockMvc.perform(post("/api/forum/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("password", "case123456", "nickName", "No Account"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 400);
    }

    @Test
    void rejectsRegisterWithShortPassword() throws Exception {
        JsonNode error = apiRoot(mockMvc.perform(post("/api/forum/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("username", "short_password_user", "password", "12345"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 400);
    }

    @Test
    void rejectsDuplicateUsername() throws Exception {
        JsonNode error = apiRoot(mockMvc.perform(post("/api/forum/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("username", "admin", "password", "case123456"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 400);
    }

    @Test
    void rejectsWrongLoginPassword() throws Exception {
        JsonNode error = apiRoot(mockMvc.perform(post("/api/forum/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("account", "admin", "password", "wrong-password"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 401);
    }

    @Test
    void rejectsProtectedEndpointWithoutToken() throws Exception {
        JsonNode error = apiRoot(mockMvc.perform(get("/api/forum/auth/me"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 401);
    }

    @Test
    void rejectsPostCreationWithoutToken() throws Exception {
        JsonNode error = apiRoot(mockMvc.perform(post("/api/forum/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("boardId", 1L, "title", "No token", "content", "Should fail"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 401);
    }

    @Test
    void rejectsPostCreationForMissingBoard() throws Exception {
        String token = registerAndToken("missing_board_user");

        JsonNode error = apiRoot(mockMvc.perform(post("/api/forum/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("boardId", 999999L, "title", "Missing board", "content", "Should fail"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 400);
    }

    @Test
    void rejectsAnonymousReadOfPendingPost() throws Exception {
        String token = registerAndToken("pending_read_user");
        Long boardId = firstBoardId();
        String postId = createPendingPost(token, boardId, "Pending read boundary").get("id").asText();

        JsonNode error = apiRoot(mockMvc.perform(get("/api/forum/posts/" + postId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 401);
    }

    @Test
    void rejectsCommentOnPendingPost() throws Exception {
        String token = registerAndToken("pending_comment_user");
        Long boardId = firstBoardId();
        String postId = createPendingPost(token, boardId, "Pending comment boundary").get("id").asText();

        JsonNode error = apiRoot(mockMvc.perform(post("/api/forum/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("content", "Comment should fail before review"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 400);
    }

    @Test
    void rejectsReportWithoutTargetId() throws Exception {
        String token = registerAndToken("report_boundary_user");

        JsonNode error = apiRoot(mockMvc.perform(post("/api/forum/reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("targetType", "POST", "reason", "Missing target"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 400);
    }

    @Test
    void rejectsFollowingSelf() throws Exception {
        JsonNode auth = register("follow_self_user");
        String token = auth.get("token").asText();
        String userId = auth.get("userId").asText();

        JsonNode error = apiRoot(mockMvc.perform(post("/api/forum/social/follow/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 400);
    }

    @Test
    void rejectsAdminEndpointForNormalUser() throws Exception {
        String token = registerAndToken("normal_admin_boundary_user");

        JsonNode error = apiRoot(mockMvc.perform(get("/api/forum/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 401);
    }

    @Test
    void handlesMalformedJsonAsBadRequest() throws Exception {
        JsonNode error = apiRoot(mockMvc.perform(post("/api/forum/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad-json"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 400);
        assertThat(error.get("msg").asText()).isEqualTo("Invalid request parameters");
    }

    @Test
    void handlesUnsupportedMethodAsBadRequest() throws Exception {
        JsonNode error = apiRoot(mockMvc.perform(delete("/api/forum/auth/login"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertBusinessError(error, 400);
        assertThat(error.get("msg").asText()).isEqualTo("Unsupported request method");
    }

    private JsonNode createPendingPost(String token, Long boardId, String title) throws Exception {
        return apiData(mockMvc.perform(post("/api/forum/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map(
                                "boardId", boardId,
                                "type", "NORMAL",
                                "title", title,
                                "content", "Boundary test content"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private Long firstBoardId() throws Exception {
        JsonNode boards = apiData(mockMvc.perform(get("/api/forum/boards"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        return boards.get(0).get("id").asLong();
    }

    private String registerAndToken(String username) throws Exception {
        return register(username).get("token").asText();
    }

    private JsonNode register(String username) throws Exception {
        return apiData(mockMvc.perform(post("/api/forum/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map(
                                "username", username,
                                "email", username + "@example.com",
                                "password", "case123456",
                                "nickName", username
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private JsonNode apiData(String responseBody) throws Exception {
        JsonNode root = apiRoot(responseBody);
        assertThat(root.get("code").asInt()).isEqualTo(200);
        return root.get("data");
    }

    private JsonNode apiRoot(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody);
    }

    private void assertBusinessError(JsonNode root, int code) {
        assertThat(root.get("code").asInt()).isEqualTo(code);
        assertThat(root.get("msg").asText()).isNotBlank();
        assertThat(root.get("data").isNull()).isTrue();
    }

    private String json(Map<String, Object> value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private Map<String, Object> map(Object... values) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }
}
