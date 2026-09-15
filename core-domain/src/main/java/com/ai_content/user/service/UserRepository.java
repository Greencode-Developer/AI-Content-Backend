package com.ai_content.user.service;

import com.ai_content.user.domain.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
}
