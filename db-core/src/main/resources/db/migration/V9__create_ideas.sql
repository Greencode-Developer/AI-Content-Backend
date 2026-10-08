CREATE TABLE ideas (
       id BIGSERIAL PRIMARY KEY,

       user_id BIGINT NOT NULL,

       platform VARCHAR(50) NOT NULL,

       title VARCHAR(255) NOT NULL,
       approach_angle TEXT,
       hook_sentence TEXT,
       reason TEXT,

       pillar_id BIGINT,
       job_id BIGINT,
       persona_id BIGINT,
       trend_signal_id BIGINT,

       source VARCHAR(50) NOT NULL,

       is_exploration BOOLEAN NOT NULL DEFAULT FALSE,
       is_used BOOLEAN NOT NULL DEFAULT FALSE,

       created_at TIMESTAMP NOT NULL,
       updated_at TIMESTAMP NOT NULL,
       deleted_at TIMESTAMP
);
