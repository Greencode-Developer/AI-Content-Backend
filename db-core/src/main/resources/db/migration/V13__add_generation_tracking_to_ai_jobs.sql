-- Links each AI job to its Langfuse trace and the prompt/model that produced it
ALTER TABLE ai_jobs
    ADD COLUMN trace_id       VARCHAR(64),
    ADD COLUMN model          VARCHAR(255), -- Bedrock inference profile ARNs exceed 100 chars
    ADD COLUMN prompt_version VARCHAR(50),
    ADD COLUMN started_at     TIMESTAMP(6),
    ADD COLUMN finished_at    TIMESTAMP(6);

-- GET /api/v1/ideas/jobs/{jobId} reads ideas by job
CREATE INDEX idx_ideas_job_id ON ideas (job_id);
