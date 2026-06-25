package com.stock.forum.dto;

import javax.validation.constraints.NotBlank;

public final class UserDtos {
    private UserDtos() {
    }

    public static class LoginRequest {
        @NotBlank
        public String code;
    }

    public static class LoginResponse {
        public String userId;
        public String nickName;
        public String avatar;
        public String token;
    }

    public static class UserInfoResponse {
        public String userId;
        public String nickName;
        public String avatar;
    }
}
