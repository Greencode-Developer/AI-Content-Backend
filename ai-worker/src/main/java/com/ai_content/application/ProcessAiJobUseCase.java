package com.ai_content.application;

import com.ai_content.infrastructure.ai.PythonAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessAiJobUseCase {

    private final PythonAiClient pythonAiClient;

    public void execute(String jobId) {
//        pythonAiClient.generate(jobId);
    }
}