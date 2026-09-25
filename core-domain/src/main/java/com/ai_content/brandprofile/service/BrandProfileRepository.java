package com.ai_content.brandprofile.service;

import com.ai_content.brandprofile.domain.BrandProfile;

import java.util.Optional;

public interface BrandProfileRepository {

    BrandProfile createEmpty(Long userId);

    Optional<BrandProfile> findByUserId(Long userId);

    Optional<BrandProfile> update(BrandProfile profile);
}