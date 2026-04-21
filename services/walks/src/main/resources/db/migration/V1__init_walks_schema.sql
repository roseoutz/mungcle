CREATE SCHEMA IF NOT EXISTS walks;
SET search_path TO walks;

CREATE TABLE walks (
    id          BIGINT PRIMARY KEY,
    dog_id      BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    type        VARCHAR(10) NOT NULL, -- OPEN, SOLO
    grid_cell   VARCHAR(30) NOT NULL,
    status      VARCHAR(10) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, ENDED
    started_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    ends_at     TIMESTAMP NOT NULL,
    ended_at    TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_walks_nearby ON walks(grid_cell, status, started_at DESC);
CREATE INDEX idx_walks_dog_status ON walks(dog_id, status);
CREATE INDEX idx_walks_user ON walks(user_id, status);

CREATE TABLE walk_patterns (
    id              BIGINT PRIMARY KEY,
    grid_cell       VARCHAR(30) NOT NULL,
    hour_of_day     INT NOT NULL CHECK (hour_of_day BETWEEN 0 AND 23),
    dog_id          BIGINT NOT NULL,
    walk_count      INT NOT NULL DEFAULT 1,
    last_walked_at  TIMESTAMP NOT NULL,
    UNIQUE(grid_cell, hour_of_day, dog_id)
);

CREATE INDEX idx_patterns_nearby ON walk_patterns(grid_cell, hour_of_day);
