CREATE SCHEMA IF NOT EXISTS identity;
SET search_path TO identity;

CREATE TABLE users (
    id          BIGINT PRIMARY KEY,
    kakao_id    VARCHAR(255) UNIQUE,
    email       VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255),
    nickname    VARCHAR(16) NOT NULL,
    neighborhood VARCHAR(100),
    push_token  VARCHAR(255),
    profile_photo_path VARCHAR(500),
    flagged_for_review BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE blocks (
    id          BIGINT PRIMARY KEY,
    blocker_id  BIGINT NOT NULL REFERENCES users(id),
    blocked_id  BIGINT NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(blocker_id, blocked_id)
);

CREATE INDEX idx_blocks_blocker ON blocks(blocker_id);
CREATE INDEX idx_blocks_blocked ON blocks(blocked_id);

CREATE TABLE reports (
    id          BIGINT PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    reported_id BIGINT NOT NULL REFERENCES users(id),
    reason      VARCHAR(500) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reports_reported ON reports(reported_id);
