package com.assis.gamelog;

import com.assis.gamelog.dto.event.GameChangedEvent;
import com.assis.gamelog.messaging.GameHistoryListener;
import com.assis.gamelog.model.GameHistory;
import com.assis.gamelog.repository.GameHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameHistoryListenerTest {

    @Mock
    private GameHistoryRepository gameHistoryRepository;

    @InjectMocks
    private GameHistoryListener gameHistoryListener;

    @Test
    void shouldSaveHistoryFromEvent() {
        GameChangedEvent event = event();
        when(gameHistoryRepository.existsByEventId("event-1")).thenReturn(false);

        gameHistoryListener.onGameChanged(event);

        ArgumentCaptor<GameHistory> captor = ArgumentCaptor.forClass(GameHistory.class);
        verify(gameHistoryRepository).save(captor.capture());
        GameHistory history = captor.getValue();
        assertEquals("event-1", history.getEventId());
        assertEquals(1L, history.getUserId());
        assertEquals(10L, history.getGameId());
        assertEquals("status", history.getFieldName());
        assertEquals("PLAYING", history.getOldValue());
        assertEquals("COMPLETED", history.getNewValue());
        assertEquals(event.getOccurredAt(), history.getChangedAt());
    }

    @Test
    void shouldIgnoreEventAlreadyProcessed() {
        when(gameHistoryRepository.existsByEventId("event-1")).thenReturn(true);

        gameHistoryListener.onGameChanged(event());

        verify(gameHistoryRepository, never()).save(any());
    }

    private GameChangedEvent event() {
        GameChangedEvent event = new GameChangedEvent();
        event.setEventId("event-1");
        event.setUserId(1L);
        event.setGameId(10L);
        event.setFieldName("status");
        event.setOldValue("PLAYING");
        event.setNewValue("COMPLETED");
        event.setOccurredAt(LocalDateTime.of(2026, 9, 27, 12, 0));
        return event;
    }
}
