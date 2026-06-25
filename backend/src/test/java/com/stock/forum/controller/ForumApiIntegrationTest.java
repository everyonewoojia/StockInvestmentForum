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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ForumApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void supportsForumCoreWorkflow() throws Exception {
        JsonNode boards = apiData(mockMvc.perform(get("/api/forum/boards"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(boards).hasSizeGreaterThanOrEqualTo(5);
        String boardId = boards.get(0).get("id").asText();

        JsonNode userLogin = apiData(mockMvc.perform(post("/api/forum/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map(
                                "username", "forum_case_user",
                                "email", "forum_case_user@example.com",
                                "password", "case123456",
                                "nickName", "Case User"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String userToken = userLogin.get("token").asText();
        assertThat(userToken).isNotBlank();

        JsonNode postData = apiData(mockMvc.perform(post("/api/forum/posts")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map(
                                "boardId", Long.valueOf(boardId),
                                "type", "LONG_ARTICLE",
                                "title", "ETF allocation checklist",
                                "summary", "A neutral checklist for ETF research.",
                                "content", "Compare fee, tracking error, liquidity and index methodology before making decisions."
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String postId = postData.get("id").asText();
        assertThat(postData.get("status").asText()).isEqualTo("PENDING_REVIEW");

        JsonNode adminLogin = apiData(mockMvc.perform(post("/api/forum/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("account", "admin", "password", "forum-admin-2026"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String adminToken = adminLogin.get("token").asText();

        JsonNode reviewData = apiData(mockMvc.perform(post("/api/forum/admin/review/posts/" + postId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("decision", "APPROVE", "reason", "Compliant"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(reviewData.get("status").asText()).isEqualTo("PUBLISHED");

        JsonNode commentData = apiData(mockMvc.perform(post("/api/forum/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("content", "Useful checklist, especially liquidity."))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(commentData.get("content").asText()).contains("liquidity");

        JsonNode interactionData = apiData(mockMvc.perform(post("/api/forum/posts/" + postId + "/interactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("action", "LIKE", "active", true))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(interactionData.get("likeCount").asInt()).isGreaterThanOrEqualTo(1);

        JsonNode reportData = apiData(mockMvc.perform(post("/api/forum/reports")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("targetType", "POST", "targetId", Long.valueOf(postId), "reason", "Compliance check", "detail", "Test report"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(reportData.get("status").asText()).isEqualTo("OPEN");

        JsonNode searchData = apiData(mockMvc.perform(get("/api/forum/search")
                        .param("keyword", "ETF"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(searchData.get("posts").get("records")).hasSizeGreaterThanOrEqualTo(1);
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
