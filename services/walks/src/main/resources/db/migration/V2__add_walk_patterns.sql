CREATE TABLE IF NOT EXISTS walks.walk_patterns (
    id          BIGINT PRIMARY KEY,
    grid_cell   VARCHAR(20) NOT NULL,
    hour_of_day INTEGER NOT NULL CHECK (hour_of_day BETWEEN 0 AND 23),
    dog_id      BIGINT NOT NULL,
    walk_count  INTEGER NOT NULL DEFAULT 1,
    last_walked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (grid_cell, hour_of_day, dog_id)
);
CREATE INDEX idx_walk_patterns_grid_hour ON walks.walk_patterns (grid_cell, hour_of_day);
