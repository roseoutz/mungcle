-- kakao_id → social_provider + social_id 마이그레이션
ALTER TABLE identity.users ADD COLUMN social_provider VARCHAR(10);
ALTER TABLE identity.users ADD COLUMN social_id VARCHAR(255);

-- 기존 카카오 데이터 마이그레이션
UPDATE identity.users SET social_provider = 'KAKAO', social_id = kakao_id WHERE kakao_id IS NOT NULL;

-- unique constraint 변경
ALTER TABLE identity.users DROP CONSTRAINT IF EXISTS users_kakao_id_key;
ALTER TABLE identity.users ADD CONSTRAINT uq_users_social UNIQUE (social_provider, social_id);

-- 기존 컬럼 제거
ALTER TABLE identity.users DROP COLUMN IF EXISTS kakao_id;

CREATE INDEX idx_users_social ON identity.users (social_provider, social_id);
