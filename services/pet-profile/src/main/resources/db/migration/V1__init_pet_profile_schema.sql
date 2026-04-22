CREATE SCHEMA IF NOT EXISTS pet_profile;
SET search_path TO pet_profile;

CREATE TABLE dogs (
    id                      BIGINT PRIMARY KEY,
    owner_id                BIGINT NOT NULL,
    name                    VARCHAR(50) NOT NULL,
    breed                   VARCHAR(100) NOT NULL,
    size                    VARCHAR(10) NOT NULL, -- SMALL, MEDIUM, LARGE
    temperaments            TEXT[] NOT NULL DEFAULT '{}',
    sociability             INT NOT NULL CHECK (sociability BETWEEN 1 AND 5),
    photo_path              VARCHAR(500),
    vaccination_photo_path  VARCHAR(500),
    deleted_at              TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_dogs_owner ON dogs(owner_id) WHERE deleted_at IS NULL;
