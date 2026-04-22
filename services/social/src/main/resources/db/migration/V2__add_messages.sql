CREATE TABLE IF NOT EXISTS social.messages (
    id              BIGINT PRIMARY KEY,
    greeting_id     BIGINT NOT NULL REFERENCES social.greetings(id),
    sender_user_id  BIGINT NOT NULL,
    body            VARCHAR(140) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_messages_greeting ON social.messages (greeting_id, created_at);
