package com.medicine.assistant.service;

import com.medicine.assistant.auth.AuthContext;
import com.medicine.assistant.common.ApiException;

public final class AuthGuard {
    private AuthGuard() {
    }

    public static Long requireSelf(String userId) {
        Long currentUserId = AuthContext.requireUserId();
        Long requestedUserId = parseId(userId, "Invalid userId");
        if (!currentUserId.equals(requestedUserId)) {
            throw ApiException.unauthorized("Cannot access another user's data");
        }
        return currentUserId;
    }

    public static Long parseId(String value, String message) {
        try {
            return Long.valueOf(value);
        } catch (Exception ex) {
            throw ApiException.badRequest(message);
        }
    }
}
