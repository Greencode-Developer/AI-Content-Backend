package com.ai_content.controller.pillar;

import com.ai_content.config.web.annotation.CurrentUser;
import com.ai_content.controller.advice.GlobalApiResponse;
import com.ai_content.controller.pillar.request.CreatePillarRequest;
import com.ai_content.pillar.domain.Pillar;
import com.ai_content.pillar.service.PillarService;
import com.ai_content.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pillars")
@RequiredArgsConstructor
public class PillarController {
    private final PillarService pillarService;

    @PostMapping
    public Pillar createPillar(@CurrentUser User user, CreatePillarRequest request){
        CreatePillarRequest createPillarRequest = CreatePillarRequest.of(
                request.name(),
                request.purpose(),
                request.lockNoReduce());
        return pillarService.createPillar(createPillarRequest.toCommand(user.id()));
    }

    @GetMapping
    public List<Pillar> getPillars(@CurrentUser User user){
        return pillarService.getPillars(user.id());
    }
}
