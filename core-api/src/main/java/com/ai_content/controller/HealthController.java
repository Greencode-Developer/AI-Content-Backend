package com.ai_content.controller;

import com.ai_content.controller.advice.GlobalApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthController {

    @GetMapping("/ping")
    public GlobalApiResponse ping() {
        return GlobalApiResponse.success(200, "pong");
    }
}