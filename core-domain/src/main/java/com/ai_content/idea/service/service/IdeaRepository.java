package com.ai_content.idea.service.service;

import com.ai_content.idea.service.domain.Idea;

import java.util.List;

public interface IdeaRepository {
    List<Idea> findAllByJobId(Long jobId);
}
