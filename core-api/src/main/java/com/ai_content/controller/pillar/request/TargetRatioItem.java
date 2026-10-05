package com.ai_content.controller.pillar.request;

import com.ai_content.pillar.command.TargetRatioItemCommand;

import java.math.BigDecimal;
import java.util.List;

public record TargetRatioItem(
        Long pillarId,
        BigDecimal targetRatio
) {
    public static List<TargetRatioItemCommand> toCommand(List<TargetRatioItem> request){
        return request.stream().map(item -> new TargetRatioItemCommand(item.pillarId(), item.targetRatio)).toList();
    }
}
