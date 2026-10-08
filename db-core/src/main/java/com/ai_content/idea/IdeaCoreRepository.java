package com.ai_content.idea;

import com.ai_content.idea.service.domain.Idea;
import com.ai_content.idea.service.service.IdeaRepository;
import com.ai_content.job.JobCoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class IdeaCoreRepository implements IdeaRepository {

    private final IdeaJpaRepository ideaJpaRepository;

    @Override
    public List<Idea> findAllByJobId(Long jobId) {
        return ideaJpaRepository.findAllByJobId(jobId)
                .stream()
                .map(idea -> Idea.of(
                        idea.getId(),
                        idea.getUserId(),
                        idea.getPlatform(),
                        idea.getTitle(),
                        idea.getApproachAngle(),
                        idea.getHookSentence(),
                        idea.getReason(),
                        idea.getPillarId(),
                        idea.getJobId(),
                        idea.getPersonaId(),
                        idea.getTrendSignalId(),
                        idea.getSource(),
                        idea.isExploration(),
                        idea.isUsed(),
                        idea.getCreatedAt(),
                        idea.getUpdatedAt(),
                        idea.getDeletedAt()
                ))
                .toList();
    }
}