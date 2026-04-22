CREATE SCHEMA IF NOT EXISTS notification;
SET search_path TO notification;

CREATE TABLE notifications (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    type        VARCHAR(30) NOT NULL,
    payload_json TEXT NOT NULL DEFAULT '{}',
    read        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_user_unread ON notifications (user_id) WHERE read = FALSE;
