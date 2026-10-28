package com.ai_content.queue;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SqsQueueProperties.class)
public class SqsQueueConfig {
}
