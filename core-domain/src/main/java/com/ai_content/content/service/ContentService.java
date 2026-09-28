package com.ai_content.content.service;

import com.ai_content.ai.AiGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ContentService {
    private final AiGenerator aiGenerator;


}
