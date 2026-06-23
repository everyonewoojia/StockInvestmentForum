package com.medicine.assistant.auth;

import com.medicine.assistant.common.ApiException;

public final class AuthContext {
    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<Long>();

    private AuthContext() {
    }

    public static void setUserId(Long userId) {
        CURRENT_USER.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_USER.get();
    }

    public static Long requireUserId() {
        Long userId = CURRENT_USER.get();
        if (userId == null) {
            throw ApiException.unauthorized("Unauthorized");
        }
        return userId;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
