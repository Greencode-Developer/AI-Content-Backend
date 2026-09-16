package com.ai_content.user;

import com.ai_content.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    void updateLastLoginAt(Long id);

    boolean existsByEmail(String email);
}