package com.assis.catalogservice;

import com.assis.catalogservice.config.RabbitConfig;
import com.assis.catalogservice.dto.CatalogGameDTO;
import com.assis.catalogservice.dto.event.CatalogGameResolvedEvent;
import com.assis.catalogservice.dto.event.GameAddedEvent;
import com.assis.catalogservice.messaging.GameAddedListener;
import com.assis.catalogservice.service.CatalogGameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameAddedListenerTest {

    @Mock
    private CatalogGameService catalogGameService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private GameAddedListener gameAddedListener;

    @Test
    void shouldResolveGameAndPublishResolvedEvent() {
        CatalogGameDTO game = new CatalogGameDTO();
        game.setRawgId(100L);
        game.setName("Elden Ring");
        game.setImageUrl("https://img/elden.jpg");
        when(catalogGameService.getGameByRawgId(100L)).thenReturn(game);

        gameAddedListener.onGameAdded(event());

        ArgumentCaptor<CatalogGameResolvedEvent> captor = ArgumentCaptor.forClass(CatalogGameResolvedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.CATALOG_EVENTS_EXCHANGE), eq(RabbitConfig.GAME_RESOLVED_ROUTING_KEY), captor.capture());
        CatalogGameResolvedEvent resolved = captor.getValue();
        assertEquals(100L, resolved.getRawgId());
        assertEquals("Elden Ring", resolved.getName());
        assertEquals("https://img/elden.jpg", resolved.getImageUrl());
        assertNotNull(resolved.getEventId());
    }

    @Test
    void shouldNotPublishWhenGameCannotBeResolved() {
        when(catalogGameService.getGameByRawgId(100L)).thenThrow(HttpClientErrorException.class);

        assertThrows(HttpClientErrorException.class, () -> gameAddedListener.onGameAdded(event()));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    private GameAddedEvent event() {
        GameAddedEvent event = new GameAddedEvent();
        event.setEventId("event-1");
        event.setRawgId(100L);
        return event;
    }
}
