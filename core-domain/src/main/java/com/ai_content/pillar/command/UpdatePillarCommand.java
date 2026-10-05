package com.ai_content.pillar.command;

public record UpdatePillarCommand(
        String name,
        String purpose,
        Boolean lockNoReduce){
    public static UpdatePillarCommand of(String name,
                                         String purpose,
                                         Boolean lockNoReduce){
        return new UpdatePillarCommand(name, purpose, lockNoReduce);
    }
}
