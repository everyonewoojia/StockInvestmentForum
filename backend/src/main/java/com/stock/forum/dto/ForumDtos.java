package com.stock.forum.dto;

import java.util.List;

public final class ForumDtos {
    private ForumDtos() {
    }

    public static class RegisterRequest {
        public String username;
        public String phone;
        public String email;
        public String password;
        public String nickName;
    }

    public static class LoginRequest {
        public String account;
        public String password;
    }

    public static class AuthResponse {
        public String userId;
        public String nickName;
        public String avatarUrl;
        public String token;
        public String role;
        public String verificationLevel;
        public boolean professionalBadge;
    }

    public static class ProfileRequest {
        public String nickName;
        public String avatarUrl;
        public String bio;
        public List<String> experienceTags;
        public List<String> markets;
        public String riskPreference;
        public String privacyProfile;
    }

    public static class BoardRequest {
        public String name;
        public String slug;
        public String category;
        public String description;
        public String market;
        public Integer sortOrder;
        public Boolean enabled;
    }

    public static class PostRequest {
        public Long boardId;
        public String type;
        public String title;
        public String summary;
        public String content;
        public List<String> stockCodes;
        public List<String> images;
        public List<String> attachments;
    }

    public static class CommentRequest {
        public Long parentId;
        public Long replyToId;
        public String content;
    }

    public static class InteractionRequest {
        public String action;
        public Boolean active;
    }

    public static class ReportRequest {
        public String targetType;
        public Long targetId;
        public String reason;
        public String detail;
    }

    public static class ReviewRequest {
        public String decision;
        public String reason;
    }

    public static class VerificationRequest {
        public String type;
        public String realName;
        public String idNumber;
        public String provider;
        public String externalRequestId;
        public List<String> materials;
    }

    public static class RiskAssessmentRequest {
        public Integer score;
        public String riskLevel;
        public List<String> answers;
    }

    public static class GroupRequest {
        public String name;
        public String description;
        public String visibility;
        public String joinPolicy;
    }

    public static class MessageRequest {
        public Long receiverId;
        public String content;
        public String imageUrl;
    }

    public static class ViolationRequest {
        public String action;
        public String reason;
        public Integer days;
    }

    public static class SensitiveWordRequest {
        public String word;
        public String category;
        public Boolean enabled;
    }
}
