CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(128) NOT NULL UNIQUE,
    session_key VARCHAR(255),
    nick_name VARCHAR(64) NOT NULL,
    avatar VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE medicines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    medicine_name VARCHAR(128) NOT NULL,
    daily_times INT NOT NULL,
    dose VARCHAR(128) NOT NULL,
    take_time VARCHAR(32) NOT NULL,
    cycle_days INT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_medicines_user_deleted (user_id, deleted)
);

CREATE TABLE reminders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    remind_times TEXT NOT NULL,
    repeat_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_reminders_user_deleted (user_id, deleted),
    INDEX idx_reminders_medicine (medicine_id)
);

CREATE TABLE records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    plan_time DATETIME NOT NULL,
    actual_time DATETIME NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_records_plan UNIQUE (user_id, medicine_id, plan_time),
    INDEX idx_records_user_plan_time (user_id, plan_time)
);

CREATE TABLE ocr_uploads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_name VARCHAR(255),
    stored_name VARCHAR(128) NOT NULL UNIQUE,
    relative_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(128),
    image_url VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_ocr_uploads_user (user_id)
);
