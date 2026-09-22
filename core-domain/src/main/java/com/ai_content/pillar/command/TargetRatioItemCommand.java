package com.ai_content.pillar.command;

import java.math.BigDecimal;

public record TargetRatioItemCommand(
        Long pillarId,
        BigDecimal targetRatio
) {
}
