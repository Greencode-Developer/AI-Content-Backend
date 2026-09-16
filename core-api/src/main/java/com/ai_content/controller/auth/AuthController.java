package com.ai_content.controller.auth;

import com.ai_content.controller.auth.request.LoginRequest;
import com.ai_content.controller.auth.request.LogoutResponse;
import com.ai_content.controller.auth.request.RefreshRequest;
import com.ai_content.controller.auth.request.RegisterRequest;
import com.ai_content.controller.auth.response.LoginResponse;
import com.ai_content.controller.auth.response.RegisterResponse;
import com.ai_content.service.auth.AuthFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return LoginResponse.from(authFacade.login(request.email(), request.password()));
    }

    @PostMapping("/register")
    public RegisterResponse login(@RequestBody @Valid RegisterRequest request) {
        return RegisterResponse.from(authFacade.register(request.fullName(),request.email(), request.password()));
    }

    @PostMapping("/logout")
    public LogoutResponse logout() {
        return LogoutResponse.from(authFacade.logout());
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestBody @Valid RefreshRequest request) {
        return LoginResponse.from(authFacade.refresh(request.refreshToken()));
    }

}
