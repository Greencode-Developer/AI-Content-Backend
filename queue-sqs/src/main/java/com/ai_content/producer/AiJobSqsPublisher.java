package com.ai_content.producer;

import com.ai_content.sqs.AiJobCommand;
import com.ai_content.sqs.AiJobPublisher;
import com.ai_content.message.AiJobMessage;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiJobSqsPublisher implements AiJobPublisher {

    private final SqsTemplate sqsTemplate;

    @Override
    public void publish(AiJobCommand command) {

        AiJobMessage message = new AiJobMessage(
                command.jobId(),
                command.userId(),
                command.type()
        );

        sqsTemplate.send(to -> to
                .queue("ai-content-job")
                .payload(message)
        );
    }
}