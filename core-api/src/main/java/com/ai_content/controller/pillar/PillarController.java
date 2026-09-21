package com.ai_content.controller.pillar;

import com.ai_content.config.web.annotation.CurrentUser;
import com.ai_content.controller.advice.GlobalApiResponse;
import com.ai_content.controller.pillar.request.CreatePillarRequest;
import com.ai_content.controller.pillar.request.UpdatePillarRequest;
import com.ai_content.pillar.domain.Pillar;
import com.ai_content.pillar.service.PillarService;
import com.ai_content.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pillars")
@RequiredArgsConstructor
public class PillarController {
    private final PillarService pillarService;

    @PostMapping
    public Pillar createPillar(@CurrentUser User user,
                               @Valid @RequestBody CreatePillarRequest request){
        return pillarService.createPillar(request.toCommand(user.id()));
    }

    @GetMapping
    public List<Pillar> getPillars(@CurrentUser User user){
        return pillarService.getPillars(user.id());
    }

    @PatchMapping("/{pillarId}")
    public Pillar updatePillar( @CurrentUser User user,
                                @PathVariable Long pillarId,
                                @RequestBody UpdatePillarRequest request){
        return pillarService.updatePillar(user.id(),pillarId,request.toCommand(request));
    }

    @DeleteMapping("/{pillarId}")
    public void deletePillar(@CurrentUser User user, @PathVariable Long pillarId){
        pillarService.deletePillar(user.id(),pillarId);
    }
}
