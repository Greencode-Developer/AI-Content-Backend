package com.ai_content.user;

import com.ai_content.user.domain.User;
import com.ai_content.user.service.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.ai_content.user.QUserEntity.userEntity;

@Repository
@RequiredArgsConstructor
public class UserCoreRepository implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(UserEntity::toDomain);
    }

    @Override
    @Transactional
    public void updateLastLoginAt(Long userId) {
        jpaQueryFactory.update(userEntity)
                .set(userEntity.lastLoginAt, LocalDateTime.now())
                .where(
                        userEntity.id.eq(userId),
                        userEntity.deletedAt.isNull()
                )
                .execute();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public User save(String fullName, String email, String encodedPassword) {
        UserEntity userEntity = userJpaRepository.save(UserEntity.createUser(fullName,email,encodedPassword));
        return UserEntity.toDomain(userEntity);
    }
}