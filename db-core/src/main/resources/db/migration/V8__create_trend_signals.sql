CREATE TABLE trend_signals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    followed_channel_id BIGINT NOT NULL,
    post_id VARCHAR(255) NOT NULL,
    title TEXT,
    url TEXT NOT NULL,
    published_at TIMESTAMP,
    likes INTEGER,
    comments INTEGER,
    shares INTEGER,
    post_format VARCHAR(20) NOT NULL,
    story_formula JSONB,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_trend_signals_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_trend_signals_followed_channel
        FOREIGN KEY (followed_channel_id)
        REFERENCES followed_channels(id),

    CONSTRAINT uq_trend_signals_channel_post
        UNIQUE (followed_channel_id, post_id),

    CONSTRAINT chk_trend_signals_post_format
        CHECK (
            post_format IN ('TEXT', 'IMAGE_TEXT', 'VIDEO_SCRIPT')
        ),

    CONSTRAINT chk_trend_signals_status
        CHECK (status IN ('ACTIVE', 'DELETED'))
);
