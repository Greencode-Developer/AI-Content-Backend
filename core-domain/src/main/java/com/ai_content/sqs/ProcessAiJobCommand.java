package com.ai_content.sqs;

public record ProcessAiJobCommand(
        Long jobId,
        Long userId,
        String type
){
}
