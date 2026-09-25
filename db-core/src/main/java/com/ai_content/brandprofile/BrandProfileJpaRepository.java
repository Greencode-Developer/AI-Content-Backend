package com.ai_content.brandprofile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BrandProfileJpaRepository
        extends JpaRepository<BrandProfileEntity, UUID> {

    Optional<BrandProfileEntity> findByUserId(Long userId);
}