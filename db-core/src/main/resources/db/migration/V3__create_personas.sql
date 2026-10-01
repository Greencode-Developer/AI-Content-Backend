CREATE TABLE personas (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age_range VARCHAR(100) NOT NULL,
    occupation VARCHAR(255) NOT NULL,
    pain_points TEXT NOT NULL,
    desires TEXT NOT NULL,
    typical_phrases TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    CONSTRAINT fk_personas_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
);
