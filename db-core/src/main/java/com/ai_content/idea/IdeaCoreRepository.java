package com.ai_content.idea;

import com.ai_content.idea.service.domain.Idea;
import com.ai_content.idea.service.domain.NewIdea;
import com.ai_content.idea.service.service.IdeaRepository;
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

    @Override
    public void saveAll(List<NewIdea> ideas) {
        ideaJpaRepository.saveAll(ideas.stream()
                .map(idea -> IdeaEntity.create(
                        idea.userId(),
                        idea.platform(),
                        idea.title(),
                        idea.approachAngle(),
                        idea.hookSentence(),
                        idea.reason(),
                        idea.pillarId(),
                        idea.jobId(),
                        idea.personaId(),
                        idea.trendSignalId(),
                        idea.source(),
                        idea.isExploration()
                ))
                .toList());
    }
}
