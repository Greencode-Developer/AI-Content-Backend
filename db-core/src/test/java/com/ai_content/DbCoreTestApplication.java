package com.ai_content;

import com.ai_content.config.JpaAuditingConfig;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.ai_content")
@EnableJpaRepositories(basePackages = "com.ai_content")
@Import(JpaAuditingConfig.class)
public class DbCoreTestApplication {
}
