CREATE SCHEMA IF NOT EXISTS social;
SET search_path TO social;

CREATE TABLE greetings (
    id                BIGINT PRIMARY KEY,
    sender_user_id    BIGINT NOT NULL,
    sender_dog_id     BIGINT NOT NULL,
    receiver_user_id  BIGINT NOT NULL,
    receiver_dog_id   BIGINT NOT NULL,
    receiver_walk_id  BIGINT NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    responded_at      TIMESTAMPTZ,
    expires_at        TIMESTAMPTZ NOT NULL,
    UNIQUE (sender_user_id, receiver_walk_id)
);
CREATE INDEX idx_greetings_receiver ON greetings (receiver_user_id, status);
CREATE INDEX idx_greetings_sender ON greetings (sender_user_id, status);
