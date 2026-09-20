CREATE TABLE content_pillars (
     id BIGSERIAL PRIMARY KEY,
     user_id BIGINT NOT NULL,
     name VARCHAR(255) NOT NULL,
     purpose TEXT,
     target_ratio NUMERIC(3, 2) NOT NULL,
     status VARCHAR(20) NOT NULL,
     lock_no_reduce BOOLEAN NOT NULL DEFAULT FALSE,

     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     deleted_at TIMESTAMP,

     CONSTRAINT fk_content_pillars_user
         FOREIGN KEY (user_id)
             REFERENCES users(id)
);