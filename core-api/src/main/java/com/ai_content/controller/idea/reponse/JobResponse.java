package com.ai_content.controller.idea.reponse;

public record JobResponse(
        Long jobId
){
    public static JobResponse of(Long jobId) {
        return new JobResponse(jobId);
    }
}
