package com.stock.forum.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CommonModelsTest {
    @Test
    void successResponseUsesUnifiedShape() {
        ApiResponse<String> response = ApiResponse.success("created", "payload");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMsg()).isEqualTo("created");
        assertThat(response.getData()).isEqualTo("payload");
    }

    @Test
    void errorResponseCarriesBusinessCodeAndMessage() {
        ApiResponse<Object> response = ApiResponse.error(401, "请先登录");

        assertThat(response.getCode()).isEqualTo(401);
        assertThat(response.getMsg()).isEqualTo("请先登录");
        assertThat(response.getData()).isNull();
    }

    @Test
    void pageResponsePreservesPaginationMetadata() {
        PageResponse<String> page = PageResponse.of(Arrays.asList("a", "b"), 12L, 2, 5);

        assertThat(page.getRecords()).containsExactly("a", "b");
        assertThat(page.getTotal()).isEqualTo(12L);
        assertThat(page.getPage()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(5);
    }

    @Test
    void apiExceptionFactoriesUseExpectedCodes() {
        assertThat(ApiException.badRequest("bad").getCode()).isEqualTo(400);
        assertThat(ApiException.unauthorized("no").getCode()).isEqualTo(401);
        assertThat(ApiException.serverError("error").getCode()).isEqualTo(500);
    }
}
