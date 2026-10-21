package com.ai_content.presentation;

import com.ai_content.application.CollectTrendSignalUseCase;
import com.ai_content.message.TrendSignalCollectionMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrendSignalCollectionListenerTest {

    @Mock
    CollectTrendSignalUseCase collectTrendSignalUseCase;

    @InjectMocks
    TrendSignalCollectionListener listener;

    private static final TrendSignalCollectionMessage MESSAGE = new TrendSignalCollectionMessage(1L);

    @Test
    void process_whenSuccess_delegatesToUseCase() {
        listener.process(MESSAGE);

        verify(collectTrendSignalUseCase).execute(1L);
    }

    @Test
    void process_whenClientError_dropsMessage() {
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST))
                .when(collectTrendSignalUseCase).execute(1L);

        assertThatCode(() -> listener.process(MESSAGE)).doesNotThrowAnyException();
    }

    @Test
    void process_whenInvalidMessage_dropsMessage() {
        TrendSignalCollectionMessage invalid = new TrendSignalCollectionMessage(null);
        doThrow(new IllegalArgumentException("followedChannelId is required"))
                .when(collectTrendSignalUseCase).execute(null);

        assertThatCode(() -> listener.process(invalid)).doesNotThrowAnyException();
    }

    @Test
    void process_whenRateLimited_rethrowsForRetry() {
        doThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS))
                .when(collectTrendSignalUseCase).execute(1L);

        assertThatThrownBy(() -> listener.process(MESSAGE))
                .isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void process_whenRequestTimeout_rethrowsForRetry() {
        doThrow(new HttpClientErrorException(HttpStatus.REQUEST_TIMEOUT))
                .when(collectTrendSignalUseCase).execute(1L);

        assertThatThrownBy(() -> listener.process(MESSAGE))
                .isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void process_whenServerError_rethrowsForRetry() {
        doThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE))
                .when(collectTrendSignalUseCase).execute(1L);

        assertThatThrownBy(() -> listener.process(MESSAGE))
                .isInstanceOf(HttpServerErrorException.class);
    }

    @Test
    void process_whenTimeout_rethrowsForRetry() {
        doThrow(new ResourceAccessException("Read timed out"))
                .when(collectTrendSignalUseCase).execute(1L);

        assertThatThrownBy(() -> listener.process(MESSAGE))
                .isInstanceOf(ResourceAccessException.class);
    }

    @Test
    void process_whenDatabaseError_rethrowsForRetry() {
        doThrow(new DataAccessResourceFailureException("DB down"))
                .when(collectTrendSignalUseCase).execute(1L);

        assertThatThrownBy(() -> listener.process(MESSAGE))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }
}
