package com.ai_content.brandprofile;

import com.ai_content.brandprofile.domain.BrandProfile;
import com.ai_content.brandprofile.service.BrandProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BrandProfileCoreRepository implements BrandProfileRepository {

    private final BrandProfileJpaRepository jpaRepository;

    @Override
    public BrandProfile createEmpty(Long userId) {
        BrandProfileEntity entity =
                BrandProfileEntity.createEmpty(userId);

        BrandProfileEntity saved =
                jpaRepository.saveAndFlush(entity);

        return saved.toDomain();
    }

    @Override
    public Optional<BrandProfile> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId)
                .map(BrandProfileEntity::toDomain);
    }

    @Override
    public Optional<BrandProfile> update(BrandProfile profile) {
        return jpaRepository.findByUserId(profile.userId())
                .filter(entity -> entity.getId().equals(profile.id()))
                .map(entity -> {
                    entity.apply(profile);
                    jpaRepository.flush();

                    return entity.toDomain();
                });
    }
}