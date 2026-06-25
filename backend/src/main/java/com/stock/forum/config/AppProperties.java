package com.stock.forum.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private Forum forum = new Forum();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public static class Jwt {
        private String secret;
        private long expirationMinutes = 10080;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(long expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }
    }

    public static class Forum {
        private ExternalServices externalServices = new ExternalServices();
        private Search search = new Search();
        private Redis redis = new Redis();

        public ExternalServices getExternalServices() {
            return externalServices;
        }

        public void setExternalServices(ExternalServices externalServices) {
            this.externalServices = externalServices;
        }

        public Search getSearch() {
            return search;
        }

        public void setSearch(Search search) {
            this.search = search;
        }

        public Redis getRedis() {
            return redis;
        }

        public void setRedis(Redis redis) {
            this.redis = redis;
        }
    }

    public static class ExternalServices {
        private String smsProvider;
        private String smsAccessKeyId;
        private String smsAccessKeySecret;
        private String smsSignName;
        private String emailHost;
        private String emailUsername;
        private String emailPassword;
        private String wechatClientId;
        private String wechatClientSecret;
        private String weiboClientId;
        private String weiboClientSecret;
        private String tencentFaceSecretId;
        private String tencentFaceSecretKey;
        private String objectStorageEndpoint;
        private String objectStorageBucket;

        public String getSmsProvider() {
            return smsProvider;
        }

        public void setSmsProvider(String smsProvider) {
            this.smsProvider = smsProvider;
        }

        public String getSmsAccessKeyId() {
            return smsAccessKeyId;
        }

        public void setSmsAccessKeyId(String smsAccessKeyId) {
            this.smsAccessKeyId = smsAccessKeyId;
        }

        public String getSmsAccessKeySecret() {
            return smsAccessKeySecret;
        }

        public void setSmsAccessKeySecret(String smsAccessKeySecret) {
            this.smsAccessKeySecret = smsAccessKeySecret;
        }

        public String getSmsSignName() {
            return smsSignName;
        }

        public void setSmsSignName(String smsSignName) {
            this.smsSignName = smsSignName;
        }

        public String getEmailHost() {
            return emailHost;
        }

        public void setEmailHost(String emailHost) {
            this.emailHost = emailHost;
        }

        public String getEmailUsername() {
            return emailUsername;
        }

        public void setEmailUsername(String emailUsername) {
            this.emailUsername = emailUsername;
        }

        public String getEmailPassword() {
            return emailPassword;
        }

        public void setEmailPassword(String emailPassword) {
            this.emailPassword = emailPassword;
        }

        public String getWechatClientId() {
            return wechatClientId;
        }

        public void setWechatClientId(String wechatClientId) {
            this.wechatClientId = wechatClientId;
        }

        public String getWechatClientSecret() {
            return wechatClientSecret;
        }

        public void setWechatClientSecret(String wechatClientSecret) {
            this.wechatClientSecret = wechatClientSecret;
        }

        public String getWeiboClientId() {
            return weiboClientId;
        }

        public void setWeiboClientId(String weiboClientId) {
            this.weiboClientId = weiboClientId;
        }

        public String getWeiboClientSecret() {
            return weiboClientSecret;
        }

        public void setWeiboClientSecret(String weiboClientSecret) {
            this.weiboClientSecret = weiboClientSecret;
        }

        public String getTencentFaceSecretId() {
            return tencentFaceSecretId;
        }

        public void setTencentFaceSecretId(String tencentFaceSecretId) {
            this.tencentFaceSecretId = tencentFaceSecretId;
        }

        public String getTencentFaceSecretKey() {
            return tencentFaceSecretKey;
        }

        public void setTencentFaceSecretKey(String tencentFaceSecretKey) {
            this.tencentFaceSecretKey = tencentFaceSecretKey;
        }

        public String getObjectStorageEndpoint() {
            return objectStorageEndpoint;
        }

        public void setObjectStorageEndpoint(String objectStorageEndpoint) {
            this.objectStorageEndpoint = objectStorageEndpoint;
        }

        public String getObjectStorageBucket() {
            return objectStorageBucket;
        }

        public void setObjectStorageBucket(String objectStorageBucket) {
            this.objectStorageBucket = objectStorageBucket;
        }
    }

    public static class Search {
        private String endpoint;
        private String indexPrefix = "forum";

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getIndexPrefix() {
            return indexPrefix;
        }

        public void setIndexPrefix(String indexPrefix) {
            this.indexPrefix = indexPrefix;
        }
    }

    public static class Redis {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
