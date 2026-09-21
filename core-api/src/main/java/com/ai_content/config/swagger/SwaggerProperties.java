package com.ai_content.config.swagger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "swagger")
public record SwaggerProperties (
        String serverUrl,
        String user,
        String password
) {}