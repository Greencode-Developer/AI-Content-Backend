package com.ai_content.controller.idea;

import com.ai_content.config.web.annotation.CurrentUser;
import com.ai_content.controller.idea.reponse.JobIdeasResponse;
import com.ai_content.controller.idea.reponse.JobResponse;
import com.ai_content.controller.idea.request.GenerateIdeaRequest;
import com.ai_content.idea.service.command.GenerateIdeaCommand;
import com.ai_content.idea.service.service.IdeaService;
import com.ai_content.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ideas")
@RequiredArgsConstructor
public class IdeaController {

    private final IdeaService ideaService;

    @PostMapping("/generate")
    public JobResponse generate(
            @CurrentUser User user,
            @RequestBody(required = false) @Valid GenerateIdeaRequest request
    ) {
        GenerateIdeaCommand command = request == null ? GenerateIdeaCommand.empty() : request.toCommand();
        return JobResponse.of(ideaService.generateIdea(user.id(), command));
    }

    @GetMapping("/jobs/{jobId}")
    public JobIdeasResponse getIdeasByJobId(
            @CurrentUser User user,
            @PathVariable Long jobId
    ) {
        return JobIdeasResponse.from(ideaService.getJobIdeas(user.id(), jobId));
    }
}
