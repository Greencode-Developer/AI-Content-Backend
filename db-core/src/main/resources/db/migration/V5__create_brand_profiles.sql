CREATE TABLE brand_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    description TEXT,
    tone_of_voice TEXT,
    forbidden_words TEXT,
    brand_colors JSONB NOT NULL DEFAULT '[]'::jsonb,
    completeness_pct INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_brand_profiles_user_id UNIQUE (user_id),

    CONSTRAINT fk_brand_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT chk_brand_profiles_completeness
        CHECK (completeness_pct BETWEEN 0 AND 100),

    CONSTRAINT chk_brand_profiles_colors_array
        CHECK (jsonb_typeof(brand_colors) = 'array')
);

-- Bổ sung hồ sơ rỗng cho những User đã tồn tại.
INSERT INTO brand_profiles (user_id)
SELECT u.id
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM brand_profiles bp
    WHERE bp.user_id = u.id
);