package com.ai_content.job;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobJpaRepository extends JpaRepository<JobEntity,Long> {
}
