package com.stock.forum.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.forum.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WechatClientTest {

    @Test
    void exchangeCodeParsesTextPlainJsonResponse() {
        AppProperties properties = new AppProperties();
        properties.getWechat().setAppid("appid");
        properties.getWechat().setSecret("secret");
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(containsString("jscode2session")))
                .andExpect(requestTo(containsString("js_code=wx-code")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"openid\":\"openid-1\",\"session_key\":\"session-key\"}", MediaType.TEXT_PLAIN));

        WechatClient client = new WechatClient(properties, restTemplate, new ObjectMapper());
        WechatClient.Session session = client.exchangeCode("wx-code");

        assertThat(session.openid).isEqualTo("openid-1");
        assertThat(session.sessionKey).isEqualTo("session-key");
        server.verify();
    }
}
