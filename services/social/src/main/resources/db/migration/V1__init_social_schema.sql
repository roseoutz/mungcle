CREATE SCHEMA IF NOT EXISTS social;
SET search_path TO social;

CREATE TABLE greetings (
    id                  BIGINT PRIMARY KEY,
    sender_user_id      BIGINT NOT NULL,
    receiver_user_id    BIGINT NOT NULL,
    sender_dog_id       BIGINT NOT NULL,
    receiver_dog_id     BIGINT,
    receiver_walk_id    BIGINT NOT NULL,
    status              VARCHAR(10) NOT NULL DEFAULT 'PENDING', -- PENDING, ACCEPTED, EXPIRED
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    responded_at        TIMESTAMP,
    expires_at          TIMESTAMP NOT NULL,
    UNIQUE(sender_user_id, receiver_user_id, receiver_walk_id)
);

CREATE INDEX idx_greetings_receiver ON greetings(receiver_user_id, status);
CREATE INDEX idx_greetings_sender ON greetings(sender_user_id, status);
CREATE INDEX idx_greetings_expiry ON greetings(status, expires_at) WHERE status IN ('PENDING', 'ACCEPTED');

CREATE TABLE messages (
    id              BIGINT PRIMARY KEY,
    greeting_id     BIGINT NOT NULL REFERENCES greetings(id),
    sender_user_id  BIGINT NOT NULL,
    body            VARCHAR(140) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_greeting ON messages(greeting_id, created_at);
