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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ForumApiEndToEndAcceptanceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completesUserAdminSocialAndOperationWorkflow() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        JsonNode author = register("author_" + suffix);
        String authorToken = author.get("token").asText();
        Long authorId = author.get("userId").asLong();

        JsonNode follower = register("follower_" + suffix);
        String followerToken = follower.get("token").asText();

        JsonNode profile = apiData(mockMvc.perform(put("/api/forum/users/me")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map(
                                "nickName", "Author " + suffix,
                                "bio", "Long-term fund and stock research",
                                "markets", new String[]{"A股", "基金"},
                                "experienceTags", new String[]{"ETF", "价值投资"},
                                "riskPreference", "BALANCED",
                                "privacyProfile", "PUBLIC"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(profile.get("nickName").asText()).contains("Author");

        JsonNode risk = apiData(mockMvc.perform(post("/api/forum/users/me/risk-assessment")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("score", 76, "riskLevel", "BALANCED", "answers", new String[]{"long-term", "medium-risk"}))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(risk.get("suitabilityStatus").asText()).isEqualTo("COMPLETED");

        JsonNode verification = apiData(mockMvc.perform(post("/api/forum/users/me/verifications")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("type", "PROFESSIONAL", "materials", new String[]{"qualification.pdf"}))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(verification.get("id").asLong()).isEqualTo(authorId);

        JsonNode group = apiData(mockMvc.perform(post("/api/forum/groups")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("name", "ETF Research " + suffix, "description", "ETF research room", "visibility", "PUBLIC", "joinPolicy", "OPEN"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        Long groupId = group.get("id").asLong();

        JsonNode join = apiData(mockMvc.perform(post("/api/forum/groups/" + groupId + "/join")
                        .header("Authorization", "Bearer " + followerToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(join.get("success").asBoolean()).isTrue();

        Long boardId = firstBoardId();
        JsonNode postData = apiData(mockMvc.perform(post("/api/forum/posts")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map(
                                "boardId", boardId,
                                "type", "LONG_ARTICLE",
                                "title", "End to end ETF checklist " + suffix,
                                "summary", "ETF checklist",
                                "content", "Compare fee, tracking error, liquidity and index rules.",
                                "stockCodes", new String[]{"510300"}
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        Long postId = postData.get("id").asLong();
        assertThat(postData.get("status").asText()).isEqualTo("PENDING_REVIEW");

        String adminToken = login("admin", "forum-admin-2026").get("token").asText();
        JsonNode reviewedPost = apiData(mockMvc.perform(post("/api/forum/admin/review/posts/" + postId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("decision", "APPROVE", "reason", "Compliant research"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(reviewedPost.get("status").asText()).isEqualTo("PUBLISHED");

        JsonNode search = apiData(mockMvc.perform(get("/api/forum/search").param("keyword", suffix))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(search.get("posts").get("records")).hasSizeGreaterThanOrEqualTo(1);

        JsonNode comment = apiData(mockMvc.perform(post("/api/forum/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + followerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("content", "Accepted end to end comment"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(comment.get("content").asText()).contains("end to end");

        JsonNode likedPost = apiData(mockMvc.perform(post("/api/forum/posts/" + postId + "/interactions")
                        .header("Authorization", "Bearer " + followerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("action", "LIKE", "active", true))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(likedPost.get("likeCount").asInt()).isGreaterThanOrEqualTo(1);

        JsonNode followed = apiData(mockMvc.perform(post("/api/forum/social/follow/" + authorId)
                        .header("Authorization", "Bearer " + followerToken)
                        .param("starred", "true"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(followed.get("success").asBoolean()).isTrue();

        JsonNode followingFeed = apiData(mockMvc.perform(get("/api/forum/social/following-feed")
                        .header("Authorization", "Bearer " + followerToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(followingFeed.get("records")).hasSizeGreaterThanOrEqualTo(1);

        JsonNode message = apiData(mockMvc.perform(post("/api/forum/messages")
                        .header("Authorization", "Bearer " + followerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("receiverId", authorId, "content", "Hello from acceptance test"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(message.get("content").asText()).contains("acceptance");

        JsonNode report = apiData(mockMvc.perform(post("/api/forum/reports")
                        .header("Authorization", "Bearer " + followerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("targetType", "POST", "targetId", postId, "reason", "Review sample", "detail", "Acceptance report"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        Long reportId = report.get("id").asLong();

        JsonNode resolved = apiData(mockMvc.perform(post("/api/forum/admin/reports/" + reportId + "/resolve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("reason", "Handled"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(resolved.get("success").asBoolean()).isTrue();
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

    private JsonNode login(String account, String password) throws Exception {
        return apiData(mockMvc.perform(post("/api/forum/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("account", account, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private Long firstBoardId() throws Exception {
        JsonNode boards = apiData(mockMvc.perform(get("/api/forum/boards"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        return boards.get(0).get("id").asLong();
    }

    private JsonNode apiData(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        assertThat(root.get("code").asInt()).isEqualTo(200);
        return root.get("data");
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
