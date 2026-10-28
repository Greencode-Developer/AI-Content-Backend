package com.ai_content.job;

import com.ai_content.job.domain.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface JobJpaRepository extends JpaRepository<JobEntity,Long> {

    // Conditional update so that only one worker can claim a job.
    // A PROCESSING job started before :staleBefore is claimable again: its worker died mid-run.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update JobEntity j
               set j.status = :processing, j.startedAt = :now, j.updatedAt = :now
             where j.id = :id
               and (j.status = :pending
                    or (j.status = :processing and coalesce(j.startedAt, j.updatedAt) < :staleBefore))
            """)
    int claim(@Param("id") Long id,
              @Param("pending") JobStatus pending,
              @Param("processing") JobStatus processing,
              @Param("now") LocalDateTime now,
              @Param("staleBefore") LocalDateTime staleBefore);
}
