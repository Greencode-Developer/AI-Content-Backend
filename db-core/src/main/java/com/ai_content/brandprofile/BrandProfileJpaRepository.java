package com.ai_content.brandprofile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandProfileJpaRepository
        extends JpaRepository<BrandProfileEntity, Long> {

    Optional<BrandProfileEntity> findByUserId(Long userId);
}