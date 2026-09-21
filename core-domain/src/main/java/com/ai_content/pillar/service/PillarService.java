package com.ai_content.pillar.service;

import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.ai_content.pillar.command.CreatePillarCommand;
import com.ai_content.pillar.domain.Pillar;
import com.ai_content.user.domain.User;
import com.ai_content.user.service.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PillarService {
    private final PillarRepository pillarRepository;

    @Transactional
    public Pillar createPillar(CreatePillarCommand createPillar){
        return pillarRepository.createPillar(
                createPillar.userId(),
                createPillar.name(),
                createPillar.purpose(),
                createPillar.lockNoReduce());
    }

    public List<Pillar> getPillars(Long userId){
        return pillarRepository.getPillars(userId);
    }
}
