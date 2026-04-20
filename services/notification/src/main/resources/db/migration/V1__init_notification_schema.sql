CREATE SCHEMA IF NOT EXISTS notification;
SET search_path TO notification;

CREATE TABLE notifications (
    id          BIGINT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    type        VARCHAR(30) NOT NULL,
    payload     JSONB NOT NULL DEFAULT '{}',
    read_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id, created_at DESC);

CREATE TABLE processed_events (
    event_id     VARCHAR(100) PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
