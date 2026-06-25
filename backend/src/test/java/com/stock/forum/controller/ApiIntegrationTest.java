package com.stock.forum.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.forum.external.TencentOcrClient;
import com.stock.forum.external.WechatClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WechatClient wechatClient;

    @MockBean
    private TencentOcrClient tencentOcrClient;

    @Test
    void supportsFrontendApiFlow() throws Exception {
        when(wechatClient.exchangeCode("wx-code")).thenReturn(new WechatClient.Session("openid-flow", "session-key"));
        when(tencentOcrClient.recognizeByBase64(anyString())).thenReturn(Arrays.asList(
                "布洛芬缓释胶囊", "每日3次", "每次1粒", "饭后服用", "疗程7天"
        ));

        JsonNode loginData = apiData(mockMvc.perform(post("/api/medicineBox/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("code", "wx-code"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String userId = loginData.get("userId").asText();
        String token = loginData.get("token").asText();
        assertThat(userId).isNotBlank();
        assertThat(token).isNotBlank();

        JsonNode unauthorized = objectMapper.readTree(mockMvc.perform(get("/api/medicineBox/medicine/list")
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(unauthorized.get("code").asInt()).isEqualTo(401);

        MockMultipartFile file = new MockMultipartFile("file", "rx.jpg", "image/jpeg", new byte[]{1, 2, 3});
        JsonNode uploadData = apiData(mockMvc.perform(multipart("/api/medicineBox/ocr/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String imageUrl = uploadData.get("imageUrl").asText();
        assertThat(imageUrl).startsWith("/api/medicineBox/ocr/file/");

        JsonNode recognizeData = apiData(mockMvc.perform(post("/api/medicineBox/ocr/recognize")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("userId", userId, "imageUrl", imageUrl))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(recognizeData.get("medicineName").asText()).isEqualTo("布洛芬缓释胶囊");

        JsonNode saveData = apiData(mockMvc.perform(post("/api/medicineBox/ocr/save")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("userId", userId, "medicineName", "布洛芬缓释胶囊", "dailyTimes", 3, "dose", "每次1粒", "takeTime", "饭后", "cycle", 7))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String medicineId = saveData.get("medicineId").asText();

        JsonNode medicineList = apiData(mockMvc.perform(get("/api/medicineBox/medicine/list")
                        .header("Authorization", "Bearer " + token)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(medicineList).hasSize(1);

        JsonNode reminderData = apiData(mockMvc.perform(post("/api/medicineBox/reminder/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("userId", userId, "medicineId", medicineId, "remindTimes", Arrays.asList("00:00"), "repeatType", "daily", "status", "enable"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String reminderId = reminderData.get("reminderId").asText();
        assertThat(reminderId).isNotBlank();

        LocalDate today = LocalDate.now();
        String start = today.atStartOfDay().format(DATE_TIME);
        String end = today.atTime(23, 59, 59).format(DATE_TIME);
        String planTime = today.atStartOfDay().format(DATE_TIME);
        String actualTime = LocalDateTime.now().format(DATE_TIME);

        JsonNode firstRecordList = apiData(mockMvc.perform(get("/api/medicineBox/record/list")
                        .header("Authorization", "Bearer " + token)
                        .param("userId", userId)
                        .param("startTime", start)
                        .param("endTime", end))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(firstRecordList).hasSize(1);

        JsonNode recordData = apiData(mockMvc.perform(post("/api/medicineBox/record/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("userId", userId, "medicineId", medicineId, "planTime", planTime, "actualTime", actualTime))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String recordId = recordData.get("recordId").asText();

        JsonNode duplicateRecordData = apiData(mockMvc.perform(post("/api/medicineBox/record/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("userId", userId, "medicineId", medicineId, "planTime", planTime, "actualTime", actualTime))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(duplicateRecordData.get("recordId").asText()).isEqualTo(recordId);

        JsonNode statData = apiData(mockMvc.perform(get("/api/medicineBox/record/stat")
                        .header("Authorization", "Bearer " + token)
                        .param("userId", userId)
                        .param("type", "week")
                        .param("date", today.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(statData.get("total").asInt()).isGreaterThanOrEqualTo(1);
        assertThat(statData.get("completed").asInt()).isEqualTo(1);
        assertThat(statData.get("medicineStats")).hasSize(1);

        JsonNode deleteData = apiData(mockMvc.perform(delete("/api/medicineBox/medicine/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("medicineId", medicineId))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(deleteData.get("success").asBoolean()).isTrue();

        JsonNode emptyMedicineList = apiData(mockMvc.perform(get("/api/medicineBox/medicine/list")
                        .header("Authorization", "Bearer " + token)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(emptyMedicineList).hasSize(0);
    }

    @Test
    void recordListKeepsPlanPendingDuringFiveMinuteGracePeriod() throws Exception {
        when(wechatClient.exchangeCode("wx-grace")).thenReturn(new WechatClient.Session("openid-grace", "session-key"));

        JsonNode loginData = apiData(mockMvc.perform(post("/api/medicineBox/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("code", "wx-grace"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String userId = loginData.get("userId").asText();
        String token = loginData.get("token").asText();

        JsonNode saveData = apiData(mockMvc.perform(post("/api/medicineBox/ocr/save")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("userId", userId, "medicineName", "阿莫西林胶囊", "dailyTimes", 1, "dose", "每次1粒", "takeTime", "饭后", "cycle", 1))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        String medicineId = saveData.get("medicineId").asText();

        LocalDateTime now = LocalDateTime.now();
        String remindTime = now.format(TIME);
        mockMvc.perform(post("/api/medicineBox/reminder/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(map("userId", userId, "medicineId", medicineId, "remindTimes", Arrays.asList(remindTime), "repeatType", "daily", "status", "enable"))))
                .andExpect(status().isOk());

        LocalDate today = now.toLocalDate();
        JsonNode records = apiData(mockMvc.perform(get("/api/medicineBox/record/list")
                        .header("Authorization", "Bearer " + token)
                        .param("userId", userId)
                        .param("startTime", today.atStartOfDay().format(DATE_TIME))
                        .param("endTime", today.atTime(23, 59, 59).format(DATE_TIME)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        assertThat(records).hasSize(1);
        assertThat(records.get(0).get("status").asText()).isEqualTo("pending");
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
