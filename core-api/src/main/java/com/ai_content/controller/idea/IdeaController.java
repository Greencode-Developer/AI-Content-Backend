package com.ai_content.controller.idea;

import com.ai_content.config.web.annotation.CurrentUser;
import com.ai_content.controller.idea.reponse.IdeaResponse;
import com.ai_content.controller.idea.reponse.JobResponse;
import com.ai_content.idea.service.service.IdeaService;
import com.ai_content.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ideas")
@RequiredArgsConstructor
public class IdeaController {

    private final IdeaService ideaService;

    @PostMapping("/generate")
    public JobResponse generate(@CurrentUser User user) {
        return JobResponse.of(ideaService.generateIdea(user.id()));
    }

    @GetMapping("/jobs/{jobId}")
    public List<IdeaResponse> getIdeasByJobId(
            @CurrentUser User user,
            @PathVariable Long jobId
    ) {
        return ideaService.getIdeasByJobId(user.id(), jobId)
                .stream()
                .map(IdeaResponse::from)
                .toList();
    }
}