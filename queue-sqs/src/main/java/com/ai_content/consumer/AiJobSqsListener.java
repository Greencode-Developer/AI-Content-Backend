package com.ai_content.consumer;

import com.ai_content.message.AiJobMessage;
import com.ai_content.sqs.ProcessAiJobCommand;
import com.ai_content.sqs.ProcessAiJobUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiJobSqsListener {

    private final ProcessAiJobUseCase useCase;

    @SqsListener("ai-content-job")
    public void listen(AiJobMessage message) {

        ProcessAiJobCommand command =
                new ProcessAiJobCommand(
                        message.jobId(),
                        message.userId(),
                        message.type()
                );

        useCase.execute(command);
    }
}

