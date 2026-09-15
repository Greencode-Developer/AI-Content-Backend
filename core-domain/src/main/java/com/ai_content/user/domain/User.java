package com.ai_content.user.domain;

public record User(
        Long id,
        String name,
        String email,
        String password,
        UserRole role,
        UserStatus status
){
    public static User of(Long id, String name, String email, String password, UserRole role, UserStatus status) {
        return new User(id, name, email, password, role, status);
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

}
