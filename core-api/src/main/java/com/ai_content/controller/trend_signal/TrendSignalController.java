package com.ai_content.controller.trend_signal;

import com.ai_content.config.web.annotation.CurrentUser;
import com.ai_content.controller.common.PagedResponse;
import com.ai_content.controller.trend_signal.request.TrendSignalSearchRequest;
import com.ai_content.controller.trend_signal.response.TrendSignalDetailResponse;
import com.ai_content.controller.trend_signal.response.TrendSignalSummaryResponse;
import com.ai_content.trend_signal.service.TrendSignalService;
import com.ai_content.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trend-signals")
@RequiredArgsConstructor
public class TrendSignalController {

    private final TrendSignalService trendSignalService;

    @GetMapping
    public PagedResponse<TrendSignalSummaryResponse> getAll(
            @CurrentUser User user,
            @ModelAttribute @Valid TrendSignalSearchRequest request,
            @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return PagedResponse.from(
                trendSignalService.search(user.id(), request.toCondition(), pageable),
                TrendSignalSummaryResponse::from
        );
    }

    @GetMapping("/{id}")
    public TrendSignalDetailResponse getOne(
            @CurrentUser User user,
            @PathVariable Long id
    ) {
        return TrendSignalDetailResponse.from(
                trendSignalService.getOne(id, user.id())
        );
    }
}
