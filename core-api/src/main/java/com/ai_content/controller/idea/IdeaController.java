package com.ai_content.controller.idea;

import com.ai_content.config.web.annotation.CurrentUser;
import com.ai_content.idea.service.IdeaService;
import com.ai_content.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ideas")
@RequiredArgsConstructor
public class IdeaController {

    private final IdeaService ideaService;

    @PostMapping("/generate")
    public JobResponse generate(@CurrentUser User user) {
        return JobResponse.of(ideaService.generateIdea(user.id()));
    }
}