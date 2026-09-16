package com.ai_content.user.service;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public User getById(Long id) {
        return userRepository
                .findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOTFOUND));
    }

    public User getByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .filter(User::isActive)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOTFOUND));
    }



    public void updateLastLoginAt(Long id) {
         userRepository.updateLastLoginAt(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User save(String fullName, String email, String encodedPassword) {
        return userRepository.save(fullName,email,encodedPassword);
    }
}