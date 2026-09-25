package com.ai_content.controller.brandprofile;

import com.ai_content.brandprofile.service.BrandProfileService;
import com.ai_content.config.web.annotation.CurrentUser;
import com.ai_content.controller.brandprofile.response.BrandProfileResponse;
import com.ai_content.user.domain.User;
import com.ai_content.controller.brandprofile.request.UpdateBrandProfileRequest;

import io.swagger.v3.oas.annotations.Parameter;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/brand-profile")
@RequiredArgsConstructor
public class BrandProfileController {

    private final BrandProfileService brandProfileService;

    @GetMapping
    public BrandProfileResponse getProfile(
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        return BrandProfileResponse.from(
                brandProfileService.getByUserId(user.id())
        );
    }

    @PutMapping
    public BrandProfileResponse updateProfile(
        @Parameter(hidden = true) @CurrentUser User user,
        @RequestBody UpdateBrandProfileRequest request
    ) {
        return BrandProfileResponse.from(
            brandProfileService.update(user.id(), request.toCommand())
        );
    }
}