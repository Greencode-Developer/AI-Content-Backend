package com.ai_content.trend_signal.service;

public interface TrendSignalCollectionPublisher {
    void publish(TrendSignalCollectionCommand command);
}
