package com.ai_content.config.security;

import com.ai_content.common.error.ErrorCode;
import com.ai_content.config.swagger.SwaggerProperties;
import com.ai_content.controller.advice.ErrorHttpStatusMapper;
import com.ai_content.controller.advice.ErrorResponse;
import com.ai_content.controller.advice.GlobalApiResponse;
import com.ai_content.user.domain.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final ErrorHttpStatusMapper errorHttpStatusMapper;
    private final SwaggerProperties swaggerProperties;

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        UserDetails user =
                User.withUsername(swaggerProperties.user())
                        .password(passwordEncoder().encode(swaggerProperties.password()))
                        .roles("SWAGGER")
                        .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityMatcher(getSwaggerUrls())
                .httpBasic(withDefaults())
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                );
        return http.build();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh"
                        ).permitAll()
                        .requestMatchers("/ping").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(
                        ex -> ex
                                .authenticationEntryPoint(
                                        (request, response, authException) -> {
                                            ErrorCode errorCode =
                                                    extractAuthErrorCode(request);
                                            writeErrorResponse(response, errorCode);
                                        }
                                )
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) ->
                                                writeErrorResponse(response, ErrorCode.USER_FORBIDDEN)
                                )
                )
                .logout(AbstractHttpConfigurer::disable)
                .cors(withDefaults());

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private ErrorCode extractAuthErrorCode(HttpServletRequest request) {
        Object value = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_CODE_ATTRIBUTE);
        if (value instanceof ErrorCode errorCode) {
            return errorCode;
        }
        return ErrorCode.USER_UNAUTHORIZED;
    }
    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        HttpStatus status = errorHttpStatusMapper.toHttpStatus(errorCode);

        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        GlobalApiResponse body = GlobalApiResponse.fail(
                status.value(),
                ErrorResponse.of(errorCode.getCode(), errorCode.getMessage())
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
    private String[] getSwaggerUrls() {
        return new String[]{
                "/swagger-ui/**",
                "/v3/api-docs/**"
        };
    }
}
