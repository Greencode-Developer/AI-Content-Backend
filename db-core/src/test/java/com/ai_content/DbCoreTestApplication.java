package com.ai_content;

import org.springframework.boot.autoconfigure.SpringBootApplication;

// db-core has no application class; @DataJpaTest needs one to bootstrap the JPA slice
@SpringBootApplication
public class DbCoreTestApplication {
}
