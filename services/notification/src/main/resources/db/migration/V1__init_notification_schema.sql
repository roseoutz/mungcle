CREATE SCHEMA IF NOT EXISTS notification;
SET search_path TO notification;

CREATE TABLE notifications (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    type        VARCHAR(30) NOT NULL, -- GREETING_RECEIVED, GREETING_ACCEPTED, MESSAGE_RECEIVED, WALK_EXPIRED
    payload     JSONB NOT NULL DEFAULT '{}',
    read_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications(user_id, read_at, created_at DESC);
