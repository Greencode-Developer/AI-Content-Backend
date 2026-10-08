package com.ai_content.idea;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IdeaJpaRepository extends JpaRepository<IdeaEntity,Long> {
    List<IdeaEntity> findAllByJobId(Long jobId);
}
