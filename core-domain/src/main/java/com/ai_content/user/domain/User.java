package com.ai_content.user.domain;

public record User(
        Long id,
        String name,
        String email,
        String password,
        Role role,
        UserStatus status
){
}
