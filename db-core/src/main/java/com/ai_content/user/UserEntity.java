package com.ai_content.user;

import com.ai_content.BaseEntity;
import com.ai_content.user.domain.UserRole;
import com.ai_content.user.domain.User;
import com.ai_content.user.domain.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Builder(access = AccessLevel.PRIVATE)
    public UserEntity(
            String name,
            String email,
            String password,
            UserRole role,
            UserStatus status,
            LocalDateTime lastLoginAt) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
    }

    public static UserEntity createUser(
            String name,
            String email,
            String password
    ) {
        return UserEntity.builder()
                .name(name)
                .email(email)
                .password(password)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public static User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getRole(),
                entity.getStatus()
        );
    }
}