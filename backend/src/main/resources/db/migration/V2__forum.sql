CREATE TABLE forum_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) UNIQUE,
    phone VARCHAR(32) UNIQUE,
    email VARCHAR(128) UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    password_salt VARCHAR(128) NOT NULL,
    oauth_provider VARCHAR(32),
    oauth_open_id VARCHAR(128),
    nick_name VARCHAR(64) NOT NULL,
    avatar_url VARCHAR(500),
    bio VARCHAR(500),
    experience_tags TEXT,
    markets TEXT,
    risk_preference VARCHAR(32) NOT NULL DEFAULT 'BALANCED',
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    verification_level VARCHAR(32) NOT NULL DEFAULT 'BASIC',
    professional_badge BOOLEAN NOT NULL DEFAULT FALSE,
    suitability_status VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED',
    privacy_profile VARCHAR(32) NOT NULL DEFAULT 'PUBLIC',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    points INT NOT NULL DEFAULT 0,
    user_level INT NOT NULL DEFAULT 1,
    post_count INT NOT NULL DEFAULT 0,
    digest_count INT NOT NULL DEFAULT 0,
    influence INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_forum_users_role (role),
    INDEX idx_forum_users_status (status)
);

CREATE TABLE forum_boards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    slug VARCHAR(64) NOT NULL UNIQUE,
    category VARCHAR(64) NOT NULL,
    description VARCHAR(500),
    market VARCHAR(32),
    sort_order INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_forum_boards_enabled_sort (enabled, sort_order)
);

CREATE TABLE forum_posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    board_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(160) NOT NULL,
    summary VARCHAR(500),
    content TEXT NOT NULL,
    images TEXT,
    attachments TEXT,
    stock_codes TEXT,
    status VARCHAR(32) NOT NULL,
    review_reason VARCHAR(500),
    digest BOOLEAN NOT NULL DEFAULT FALSE,
    like_count INT NOT NULL DEFAULT 0,
    favorite_count INT NOT NULL DEFAULT 0,
    share_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    published_at DATETIME,
    INDEX idx_forum_posts_board_status (board_id, status),
    INDEX idx_forum_posts_author_status (author_id, status),
    INDEX idx_forum_posts_created (created_at),
    INDEX idx_forum_posts_published (published_at)
);

CREATE TABLE forum_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT,
    reply_to_id BIGINT,
    content TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    like_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_forum_comments_post (post_id, created_at),
    INDEX idx_forum_comments_parent (parent_id)
);

CREATE TABLE forum_interactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_forum_interactions UNIQUE (user_id, target_type, target_id, action),
    INDEX idx_forum_interactions_target (target_type, target_id, action, active)
);

CREATE TABLE forum_follows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    starred BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    CONSTRAINT uk_forum_follows UNIQUE (follower_id, following_id),
    INDEX idx_forum_follows_following (following_id)
);

CREATE TABLE forum_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    reason VARCHAR(128) NOT NULL,
    detail VARCHAR(1000),
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    handled_by BIGINT,
    handled_at DATETIME,
    created_at DATETIME NOT NULL,
    INDEX idx_forum_reports_status (status, created_at)
);

CREATE TABLE forum_sensitive_words (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(128) NOT NULL UNIQUE,
    category VARCHAR(64) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL
);

CREATE TABLE forum_verifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    real_name VARCHAR(64),
    id_number VARCHAR(64),
    provider VARCHAR(64),
    external_request_id VARCHAR(128),
    materials TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    review_reason VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_forum_verifications_user_type (user_id, type)
);

CREATE TABLE forum_risk_assessments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    score INT NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    answers TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED',
    created_at DATETIME NOT NULL,
    INDEX idx_forum_risk_user (user_id, created_at)
);

CREATE TABLE forum_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    visibility VARCHAR(32) NOT NULL DEFAULT 'PUBLIC',
    join_policy VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    member_count INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_forum_groups_visibility (visibility, created_at)
);

CREATE TABLE forum_group_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    CONSTRAINT uk_forum_group_members UNIQUE (group_id, user_id)
);

CREATE TABLE forum_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    read_at DATETIME,
    created_at DATETIME NOT NULL,
    INDEX idx_forum_messages_pair (sender_id, receiver_id, created_at),
    INDEX idx_forum_messages_receiver (receiver_id, read_at)
);

CREATE TABLE forum_notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(160) NOT NULL,
    content VARCHAR(1000),
    read_at DATETIME,
    created_at DATETIME NOT NULL,
    INDEX idx_forum_notifications_user (user_id, read_at, created_at)
);

CREATE TABLE forum_audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    detail VARCHAR(1000),
    created_at DATETIME NOT NULL,
    INDEX idx_forum_audit_target (target_type, target_id)
);

CREATE TABLE forum_stock_symbols (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    market VARCHAR(32) NOT NULL,
    aliases VARCHAR(500),
    INDEX idx_forum_stock_name (name)
);

INSERT INTO forum_boards (name, slug, category, description, market, sort_order, enabled, created_at, updated_at) VALUES
('A股市场', 'a-share', '市场讨论区', '沪深京市场热点、个股和交易制度讨论', 'A股', 10, TRUE, NOW(), NOW()),
('港股市场', 'hk-stock', '市场讨论区', '港股公司、南向资金和行业机会讨论', '港股', 20, TRUE, NOW(), NOW()),
('美股市场', 'us-stock', '市场讨论区', '美股公司、ETF、宏观和财报讨论', '美股', 30, TRUE, NOW(), NOW()),
('基金投资', 'fund', '主题专区', '主动基金、指数基金、FOF 和基金组合讨论', '基金', 40, TRUE, NOW(), NOW()),
('价值投资', 'value-investing', '主题专区', '企业价值、估值框架和长期主义讨论', NULL, 50, TRUE, NOW(), NOW()),
('量化投资', 'quant', '主题专区', '因子、回测、程序化交易和策略复盘', NULL, 60, TRUE, NOW(), NOW()),
('新股新债', 'ipo-bond', '主题专区', '新股申购、可转债和打新策略讨论', NULL, 70, TRUE, NOW(), NOW()),
('宏观策略', 'macro', '主题专区', '宏观数据、政策周期和资产配置讨论', NULL, 80, TRUE, NOW(), NOW()),
('公司研究', 'company-research', '公司研究专区', '行业与个股深度研究', NULL, 90, TRUE, NOW(), NOW()),
('问答求助', 'qa', '问答求助区', '新手提问、投资困惑和工具使用交流', NULL, 100, TRUE, NOW(), NOW());

INSERT INTO forum_sensitive_words (word, category, enabled, created_at) VALUES
('稳赚', '合规风险', TRUE, NOW()),
('内幕消息', '合规风险', TRUE, NOW()),
('操纵市场', '合规风险', TRUE, NOW()),
('保本收益', '合规风险', TRUE, NOW());

INSERT INTO forum_stock_symbols (code, name, market, aliases) VALUES
('600519', '贵州茅台', 'A股', '茅台'),
('000300', '沪深300', 'A股', 'CSI300'),
('00700', '腾讯控股', '港股', '腾讯'),
('AAPL', '苹果公司', '美股', 'Apple'),
('SPY', '标普500ETF', '美股', 'S&P 500 ETF'),
('510300', '沪深300ETF', '基金', '300ETF');
