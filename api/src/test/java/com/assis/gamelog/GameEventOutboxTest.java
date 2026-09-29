package com.assis.gamelog;

import com.assis.gamelog.dto.event.GameAddedEvent;
import com.assis.gamelog.dto.event.GameChangedEvent;
import com.assis.gamelog.messaging.GameEventOutbox;
import com.assis.gamelog.model.OutboxEvent;
import com.assis.gamelog.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GameEventOutboxTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private GameEventOutbox gameEventOutbox;

    @BeforeEach
    void setUp() {
        gameEventOutbox = new GameEventOutbox(outboxEventRepository, JsonMapper.builder().build());
    }

    @Test
    void shouldUseFieldNameInRoutingKey() {
        gameEventOutbox.register(changedEvent("rating", "5"));

        OutboxEvent saved = captureSaved();
        assertEquals("game.rating.changed", saved.getRoutingKey());
        assertEquals("event-1", saved.getEventId());
        assertTrue(saved.getPayload().contains("\"fieldName\":\"rating\""));
        assertNull(saved.getPublishedAt());
    }

    @Test
    void shouldUseDeletedRoutingKeyWhenGameIsDeleted() {
        gameEventOutbox.register(changedEvent("status", "DELETED"));

        assertEquals("game.deleted", captureSaved().getRoutingKey());
    }

    @Test
    void shouldUseGameAddedRoutingKey() {
        GameAddedEvent event = new GameAddedEvent();
        event.setEventId("event-2");
        event.setRawgId(100L);

        gameEventOutbox.register(event);

        OutboxEvent saved = captureSaved();
        assertEquals("game.added", saved.getRoutingKey());
        assertTrue(saved.getPayload().contains("\"rawgId\":100"));
    }

    private GameChangedEvent changedEvent(String fieldName, String newValue) {
        GameChangedEvent event = new GameChangedEvent();
        event.setEventId("event-1");
        event.setUserId(1L);
        event.setGameId(10L);
        event.setFieldName(fieldName);
        event.setNewValue(newValue);
        return event;
    }

    private OutboxEvent captureSaved() {
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        return captor.getValue();
    }
}
