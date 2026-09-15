package com.ai_content.config.jwt;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.user.domain.Role;
import com.ai_content.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    public String createAccessToken(User user) {
        return createToken(user, TOKEN_TYPE_ACCESS, jwtProperties.accessTokenValidity());
    }

    public String createRefreshToken(User user) {
        return createToken(user, TOKEN_TYPE_REFRESH, jwtProperties.refreshTokenValidity());
    }

    public TokenPayload parseAccessToken(String token) {
        return parseToken(token, TOKEN_TYPE_ACCESS);
    }

    public TokenPayload parseRefreshToken(String token) {
        return parseToken(token, TOKEN_TYPE_REFRESH);
    }

    private String createToken(User user, String tokenType ,long validity) {
        if (user.id() == null) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + validity);

        return Jwts.builder()
                .setSubject(String.valueOf(user.id()))
                .claim(CLAIM_ROLE, user.role().name())
                .claim(CLAIM_TYPE, tokenType)
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(getSigningKey())
                .compact();
    }

    private TokenPayload parseToken(String token, String expectedTokenType) {
        Claims claims = parseClaims(token);

        try {
            Long userId = Long.valueOf(claims.getSubject());
            String role = claims.get(CLAIM_ROLE, String.class);
            String tokenType = claims.get(CLAIM_TYPE, String.class);

            if (userId==null || role == null || tokenType == null) {
                throw new CustomException(ErrorCode.USER_TOKEN_INVALID);
            }
            if (!expectedTokenType.equals(tokenType)) {
                throw new CustomException(ErrorCode.USER_TOKEN_INVALID);
            }

            Role userRole = Role.valueOf(role);
            return new TokenPayload(userId, userRole, tokenType);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.USER_TOKEN_INVALID);
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.USER_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.USER_TOKEN_INVALID);
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
    }

}
