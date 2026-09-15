package com.ai_content.user.domain;

public record User(
        Long id,
        String name,
        String email,
        String password,
        Role role,
        UserStatus status
){
    public static User of(Long id, String name, String email, String password, Role role, UserStatus status) {
        return new User(id, name, email, password, role, status);
    }
}
