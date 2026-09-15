package com.ai_content.controller.user;

import com.ai_content.config.web.annotation.CurrentUser;
import com.ai_content.controller.advice.GlobalApiResponse;
import com.ai_content.user.domain.User;
import com.ai_content.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/test")
    public GlobalApiResponse test(@CurrentUser User user){
        return GlobalApiResponse.success(200, "user authenticated");
    }

}