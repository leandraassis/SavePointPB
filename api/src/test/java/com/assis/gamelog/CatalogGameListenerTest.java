package com.assis.gamelog;

import com.assis.gamelog.dto.event.CatalogGameResolvedEvent;
import com.assis.gamelog.messaging.CatalogGameListener;
import com.assis.gamelog.model.Game;
import com.assis.gamelog.model.GameStatus;
import com.assis.gamelog.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogGameListenerTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private CatalogGameListener catalogGameListener;

    @Test
    void shouldUpdateNameAndImageOfAllGamesWithSameRawgId() {
        Game fromUser1 = game(1L, "elden ring");
        Game fromUser2 = game(2L, "Elden");
        when(gameRepository.findByRawgId(100L)).thenReturn(List.of(fromUser1, fromUser2));

        catalogGameListener.onCatalogGameResolved(event());

        assertEquals("Elden Ring", fromUser1.getName());
        assertEquals("Elden Ring", fromUser2.getName());
        assertEquals("https://img/elden.jpg", fromUser1.getImageUrl());
        assertEquals("https://img/elden.jpg", fromUser2.getImageUrl());
        verify(gameRepository).saveAll(List.of(fromUser1, fromUser2));
    }

    @Test
    void shouldDoNothingWhenNoGameHasRawgId() {
        when(gameRepository.findByRawgId(100L)).thenReturn(List.of());

        catalogGameListener.onCatalogGameResolved(event());

        verify(gameRepository).saveAll(List.of());
    }

    private CatalogGameResolvedEvent event() {
        CatalogGameResolvedEvent event = new CatalogGameResolvedEvent();
        event.setEventId("event-1");
        event.setRawgId(100L);
        event.setName("Elden Ring");
        event.setImageUrl("https://img/elden.jpg");
        return event;
    }

    private Game game(Long userId, String name) {
        return Game.builder()
                .userId(userId)
                .rawgId(100L)
                .name(name)
                .status(GameStatus.PLAYING)
                .build();
    }
}
