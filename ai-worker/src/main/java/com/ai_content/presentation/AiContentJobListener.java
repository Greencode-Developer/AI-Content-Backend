package com.ai_content.presentation;

import com.ai_content.application.ProcessAiJobUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiContentJobListener {

    private final ProcessAiJobUseCase processAiJobUseCase;

    @SqsListener("ai-content-job")
    public void process(String jobId) {
        processAiJobUseCase.execute(jobId);
    }
}