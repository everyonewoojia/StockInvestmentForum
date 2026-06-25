package com.stock.forum.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.forum.common.ApiException;
import com.stock.forum.config.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WechatClient {
    private static final String CODE2_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private final AppProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WechatClient(AppProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Session exchangeCode(String code) {
        if (!StringUtils.hasText(properties.getWechat().getAppid()) || !StringUtils.hasText(properties.getWechat().getSecret())) {
            throw ApiException.serverError("Wechat credentials are not configured");
        }
        String url = UriComponentsBuilder.fromHttpUrl(CODE2_SESSION_URL)
                .queryParam("appid", properties.getWechat().getAppid())
                .queryParam("secret", properties.getWechat().getSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();
        String responseBody = restTemplate.getForObject(url, String.class);
        if (!StringUtils.hasText(responseBody)) {
            throw ApiException.serverError("Wechat login failed");
        }
        WechatSessionResponse response = parseResponse(responseBody);
        if (response.errcode != null && response.errcode != 0) {
            if (response.errcode == 40029 || response.errcode == 40163) {
                throw ApiException.badRequest("Invalid Wechat login code");
            }
            throw ApiException.serverError("Wechat login failed: " + response.errmsg);
        }
        if (!StringUtils.hasText(response.openid)) {
            throw ApiException.serverError("Wechat login did not return openid");
        }
        return new Session(response.openid, response.session_key);
    }

    private WechatSessionResponse parseResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, WechatSessionResponse.class);
        } catch (Exception ex) {
            throw ApiException.serverError("Wechat login response is invalid");
        }
    }

    public static class Session {
        public final String openid;
        public final String sessionKey;

        public Session(String openid, String sessionKey) {
            this.openid = openid;
            this.sessionKey = sessionKey;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WechatSessionResponse {
        public String openid;
        public String session_key;
        public Integer errcode;
        public String errmsg;
    }
}
