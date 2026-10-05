CREATE TABLE ai_jobs (
     id BIGINT NOT NULL AUTO_INCREMENT,
     user_id BIGINT NOT NULL,
     type VARCHAR(50) NOT NULL,
     status VARCHAR(50) NOT NULL,
     error_message TEXT,

     created_at DATETIME(6) NOT NULL,
     updated_at DATETIME(6) NOT NULL,
     deleted_at DATETIME(6),

     PRIMARY KEY (id)
);