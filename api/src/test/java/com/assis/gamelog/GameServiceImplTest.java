package com.assis.gamelog;

import com.assis.gamelog.client.CatalogServiceClient;
import com.assis.gamelog.dto.catalog.CatalogGameDTO;
import com.assis.gamelog.dto.event.GameAddedEvent;
import com.assis.gamelog.dto.event.GameChangedEvent;
import com.assis.gamelog.dto.request.CreateGameDTO;
import com.assis.gamelog.dto.request.UpdateGameDTO;
import com.assis.gamelog.dto.response.GameHistoryDTO;
import com.assis.gamelog.dto.response.GameResponseDTO;
import com.assis.gamelog.exception.GameAlreadyExistsException;
import com.assis.gamelog.exception.GameNotFoundException;
import com.assis.gamelog.messaging.GameEventOutbox;
import com.assis.gamelog.model.Game;
import com.assis.gamelog.model.GameHistory;
import com.assis.gamelog.model.GameStatus;
import com.assis.gamelog.repository.GameHistoryRepository;
import com.assis.gamelog.repository.GameRepository;
import com.assis.gamelog.service.impl.GameServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private CatalogServiceClient catalogServiceClient;

    @Mock
    private GameHistoryRepository gameHistoryRepository;

    @Mock
    private GameEventOutbox gameEventOutbox;

    @InjectMocks
    private GameServiceImpl gameService;

    @Test
    void shouldAddGameWithRequestDataAndRegisterGameAddedEvent() {
        CreateGameDTO dto = createDTO(100L, "Elden Ring");
        when(gameRepository.existsByUserIdAndRawgId(1L, 100L)).thenReturn(false);
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameResponseDTO response = gameService.addGame(1L, dto);

        assertEquals("Elden Ring", response.getName());
        assertEquals(GameStatus.PLAYING, response.getStatus());
        verify(catalogServiceClient, never()).getGameByRawgId(any());

        ArgumentCaptor<GameAddedEvent> captor = ArgumentCaptor.forClass(GameAddedEvent.class);
        verify(gameEventOutbox).register(captor.capture());
        assertEquals(100L, captor.getValue().getRawgId());
        assertNotNull(captor.getValue().getEventId());
    }

    @Test
    void shouldAddGameFromCatalogWhenNameIsMissing() {
        CreateGameDTO dto = createDTO(100L, null);
        CatalogGameDTO catalogGame = new CatalogGameDTO();
        catalogGame.setRawgId(100L);
        catalogGame.setName("Elden Ring");
        catalogGame.setImageUrl("https://img/elden.jpg");
        when(gameRepository.existsByUserIdAndRawgId(1L, 100L)).thenReturn(false);
        when(catalogServiceClient.getGameByRawgId(100L)).thenReturn(catalogGame);
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameResponseDTO response = gameService.addGame(1L, dto);

        assertEquals("Elden Ring", response.getName());
        assertEquals("https://img/elden.jpg", response.getImageUrl());
        verify(gameEventOutbox, never()).register(any(GameAddedEvent.class));
    }

    @Test
    void shouldThrowWhenGameAlreadyExists() {
        when(gameRepository.existsByUserIdAndRawgId(1L, 100L)).thenReturn(true);

        assertThrows(GameAlreadyExistsException.class, () -> gameService.addGame(1L, createDTO(100L, "Elden Ring")));
        verify(gameRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenGameBelongsToAnotherUser() {
        when(gameRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> gameService.getGameById(2L, 10L));
    }

    @Test
    void shouldUpdateGameAndRegisterOneEventPerChangedField() {
        Game game = game();
        when(gameRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(game));
        when(gameRepository.save(game)).thenReturn(game);

        UpdateGameDTO dto = new UpdateGameDTO();
        dto.setStatus(GameStatus.COMPLETED);
        dto.setRating(5);
        GameResponseDTO response = gameService.updateGame(1L, 10L, dto);

        assertEquals(GameStatus.COMPLETED, response.getStatus());
        assertEquals(5, response.getRating());

        ArgumentCaptor<GameChangedEvent> captor = ArgumentCaptor.forClass(GameChangedEvent.class);
        verify(gameEventOutbox, times(2)).register(captor.capture());
        List<GameChangedEvent> events = captor.getAllValues();
        assertEquals("status", events.get(0).getFieldName());
        assertEquals("PLAYING", events.get(0).getOldValue());
        assertEquals("COMPLETED", events.get(0).getNewValue());
        assertEquals("rating", events.get(1).getFieldName());
        assertNull(events.get(1).getOldValue());
        assertEquals("5", events.get(1).getNewValue());
    }

    @Test
    void shouldNotRegisterEventWhenValueDoesNotChange() {
        Game game = game();
        when(gameRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(game));
        when(gameRepository.save(game)).thenReturn(game);

        UpdateGameDTO dto = new UpdateGameDTO();
        dto.setStatus(GameStatus.PLAYING);
        gameService.updateGame(1L, 10L, dto);

        verify(gameEventOutbox, never()).register(any(GameChangedEvent.class));
    }

    @Test
    void shouldDeleteGameAndRegisterDeletedEvent() {
        Game game = game();
        when(gameRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(game));

        gameService.deleteGame(1L, 10L);

        verify(gameRepository).delete(game);
        ArgumentCaptor<GameChangedEvent> captor = ArgumentCaptor.forClass(GameChangedEvent.class);
        verify(gameEventOutbox).register(captor.capture());
        assertEquals("DELETED", captor.getValue().getNewValue());
    }

    @Test
    void shouldAddGameFromCatalogWhenNameIsBlank() {
        CatalogGameDTO catalogGame = new CatalogGameDTO();
        catalogGame.setRawgId(100L);
        catalogGame.setName("Elden Ring");
        when(gameRepository.existsByUserIdAndRawgId(1L, 100L)).thenReturn(false);
        when(catalogServiceClient.getGameByRawgId(100L)).thenReturn(catalogGame);
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameResponseDTO response = gameService.addGame(1L, createDTO(100L, "  "));

        assertEquals("Elden Ring", response.getName());
    }

    @Test
    void shouldListOnlyGamesOfUser() {
        when(gameRepository.findByUserId(1L)).thenReturn(List.of(game()));

        List<GameResponseDTO> games = gameService.getAllGames(1L);

        assertEquals(1, games.size());
        assertEquals(10L, games.get(0).getId());
        assertEquals("Elden Ring", games.get(0).getName());
    }

    @Test
    void shouldReturnGameHistoryOfUser() {
        GameHistory history = GameHistory.builder()
                .userId(1L)
                .gameId(10L)
                .fieldName("status")
                .oldValue("PLAYING")
                .newValue("COMPLETED")
                .changedAt(LocalDateTime.of(2026, 9, 27, 12, 0))
                .build();
        when(gameHistoryRepository.findByUserIdAndGameIdOrderByChangedAtDesc(1L, 10L)).thenReturn(List.of(history));

        List<GameHistoryDTO> result = gameService.getGameHistory(1L, 10L);

        assertEquals(1, result.size());
        assertEquals("status", result.get(0).getFieldName());
        assertEquals("PLAYING", result.get(0).getOldValue());
        assertEquals("COMPLETED", result.get(0).getNewValue());
        assertEquals(history.getChangedAt(), result.get(0).getChangedAt());
    }

    private CreateGameDTO createDTO(Long rawgId, String name) {
        CreateGameDTO dto = new CreateGameDTO();
        dto.setRawgId(rawgId);
        dto.setName(name);
        dto.setStatus(GameStatus.PLAYING);
        return dto;
    }

    private Game game() {
        return Game.builder()
                .id(10L)
                .userId(1L)
                .rawgId(100L)
                .name("Elden Ring")
                .status(GameStatus.PLAYING)
                .build();
    }
}
