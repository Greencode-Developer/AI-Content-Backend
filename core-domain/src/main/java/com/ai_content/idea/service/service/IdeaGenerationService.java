package com.ai_content.idea.service.service;

import com.ai_content.idea.service.domain.AiIdeaGenerationResult;
import com.ai_content.idea.service.domain.GeneratedIdea;
import com.ai_content.idea.service.domain.IdeaGenerationInput;
import com.ai_content.job.domain.Job;
import com.ai_content.job.service.AiJobRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/*
 * Runs one GENERATE_IDEA job (called by the AI worker):
 *   1. claim       PENDING → PROCESSING (only one worker wins, duplicate messages are skipped)
 *                  a job PROCESSING for longer than processingTimeout is claimed again (worker crashed);
 *                  a younger PROCESSING job → JobInProgressException so SQS redelivers it later
 *   2. generate    call the AI service, outside any transaction
 *   3. save        ideas + COMPLETED in one transaction
 *   on failure     FAILED + error message + trace id; not retried, the user can generate again
 *
 * Not @Transactional on purpose: each step commits on its own.
 * Not a @Service: AiIdeaGenerator only exists in the AI worker, so the worker declares this bean
 * (core-api scans the same packages and would fail to start).
 */
@RequiredArgsConstructor
public class IdeaGenerationService {

    private static final Logger log = LoggerFactory.getLogger(IdeaGenerationService.class);

    static final int MAX_TITLE_LENGTH = 255;

    private final AiJobRepository aiJobRepository;
    private final AiIdeaGenerator aiIdeaGenerator;
    private final IdeaGenerationInputCodec inputCodec;
    private final IdeaGenerationResultWriter resultWriter;
    // Must outlast the AI call, otherwise a live job could be claimed twice
    private final Duration processingTimeout;

    public void process(Long jobId) {
        if (!aiJobRepository.markProcessing(jobId, LocalDateTime.now().minus(processingTimeout))) {
            Optional<Job> current = aiJobRepository.findById(jobId);
            if (current.isPresent() && current.get().isProcessing()) {
                throw new JobInProgressException(jobId);
            }
            log.info("AI job {} is missing or already handled, skipping", jobId);
            return;
        }

        Job job = aiJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("AI job " + jobId + " disappeared after claim"));

        String traceId = null;
        try {
            IdeaGenerationInput input = inputCodec.fromJson(job.input());
            AiIdeaGenerationResult result = aiIdeaGenerator.generate(job.id(), job.userId(), input);
            traceId = result.meta().traceId();

            validate(result);
            resultWriter.save(job, input, result);

            log.info("AI job {} completed with {} ideas, trace {}", jobId, result.ideas().size(), traceId);
        } catch (AiGenerationException e) {
            fail(jobId, e.getMessage(), e.getTraceId() != null ? e.getTraceId() : traceId, e);
        } catch (RuntimeException e) {
            fail(jobId, "Unexpected error: " + e.getMessage(), traceId, e);
        }
    }

    private void validate(AiIdeaGenerationResult result) {
        String traceId = result.meta().traceId();
        if (result.ideas().isEmpty()) {
            throw new AiGenerationException("AI returned no ideas", traceId);
        }
        for (GeneratedIdea idea : result.ideas()) {
            if (idea == null || idea.title() == null || idea.title().isBlank()) {
                throw new AiGenerationException("AI returned an idea without title", traceId);
            }
            if (idea.title().trim().length() > MAX_TITLE_LENGTH) {
                throw new AiGenerationException("AI returned a title longer than " + MAX_TITLE_LENGTH, traceId);
            }
        }
    }

    private void fail(Long jobId, String message, String traceId, Exception e) {
        log.warn("AI job {} failed (trace {}): {}", jobId, traceId, message, e);
        aiJobRepository.markFailed(jobId, message, traceId);
    }
}
