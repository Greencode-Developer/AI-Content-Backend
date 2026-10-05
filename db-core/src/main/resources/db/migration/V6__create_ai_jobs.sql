CREATE TABLE ai_jobs (
     id BIGSERIAL PRIMARY KEY,
     user_id BIGINT NOT NULL,
     type VARCHAR(50) NOT NULL,
     status VARCHAR(50) NOT NULL,
     error_message TEXT,

     created_at TIMESTAMP(6) NOT NULL,
     updated_at TIMESTAMP(6) NOT NULL,
     deleted_at TIMESTAMP(6)
);