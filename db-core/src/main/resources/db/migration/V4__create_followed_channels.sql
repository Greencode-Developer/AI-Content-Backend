CREATE TABLE followed_channels
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    platform        VARCHAR(20)  NOT NULL,
    channel_url     VARCHAR(500) NOT NULL,
    display_name    VARCHAR(255) NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    last_fetched_at TIMESTAMP,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',

    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP,

    CONSTRAINT uq_followed_channels_user_url UNIQUE (user_id, channel_url)
);
