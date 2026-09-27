package com.assis.gamelog.service.impl;

import com.assis.gamelog.dto.catalog.CatalogGameDTO;
import com.assis.gamelog.dto.event.GameAddedEvent;
import com.assis.gamelog.dto.event.GameChangedEvent;
import com.assis.gamelog.dto.request.CreateGameDTO;
import com.assis.gamelog.dto.request.UpdateGameDTO;
import com.assis.gamelog.dto.response.GameHistoryDTO;
import com.assis.gamelog.dto.response.GameResponseDTO;
import com.assis.gamelog.exception.GameAlreadyExistsException;
import com.assis.gamelog.exception.GameNotFoundException;
import com.assis.gamelog.client.CatalogServiceClient;
import com.assis.gamelog.messaging.GameEventOutbox;
import com.assis.gamelog.model.Game;
import com.assis.gamelog.model.GameHistory;
import com.assis.gamelog.repository.GameHistoryRepository;
import com.assis.gamelog.repository.GameRepository;
import com.assis.gamelog.service.GameService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final CatalogServiceClient catalogServiceClient;
    private final GameHistoryRepository gameHistoryRepository;
    private final GameEventOutbox gameEventOutbox;

    @Override
    @Transactional
    public GameResponseDTO addGame(Long userId, CreateGameDTO dto) {
        if(gameRepository.existsByUserIdAndRawgId(userId, dto.getRawgId())) throw new GameAlreadyExistsException("Game already exists");

        if(dto.getName() == null || dto.getName().isBlank()) return addGameFromCatalog(userId, dto);

        Game game = Game.builder().userId(userId)
                .rawgId(dto.getRawgId())
                .name(dto.getName())
                .imageUrl(dto.getImageUrl())
                .status(dto.getStatus())
                .rating(dto.getRating()).build();

        Game savedGame = gameRepository.save(game);

        GameAddedEvent event = new GameAddedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setRawgId(savedGame.getRawgId());
        gameEventOutbox.register(event);

        return toResponseDTO(savedGame);
    }

    @Override
    public List<GameResponseDTO> getAllGames(Long userId) {
        return gameRepository.findByUserId(userId).stream().map(this::toResponseDTO).toList();
    }

    @Override
    public GameResponseDTO getGameById(Long userId, Long id) {
        return toResponseDTO(findGameById(userId, id));
    }

    @Override
    @Transactional
    public GameResponseDTO updateGame(Long userId, Long id, UpdateGameDTO dto) {
        Game game = findGameById(userId, id);
        if(dto.getStatus() != null) {
            logChange(game, "status", game.getStatus(), dto.getStatus());
            game.setStatus(dto.getStatus());
        }
        if(dto.getRating() != null) {
            logChange(game, "rating", game.getRating(), dto.getRating());
            game.setRating(dto.getRating());
        }

        Game updatedGame = gameRepository.save(game);
        return toResponseDTO(updatedGame);
    }

    @Override
    @Transactional
    public void deleteGame(Long userId, Long id) {
        Game game = findGameById(userId, id);
        logChange(game, "status", game.getStatus(), "DELETED");
        gameRepository.delete(game);
    }

    @Override
    public List<GameHistoryDTO> getGameHistory(Long userId, Long id) {
        return gameHistoryRepository.findByUserIdAndGameIdOrderByChangedAtDesc(userId, id).stream()
                .map(this::toHistoryDTO)
                .toList();
    }

    private void logChange(Game game, String fieldName, Object oldValue, Object newValue) {
        boolean changed = !Objects.equals(oldValue, newValue);

        if (!changed) return;

        GameChangedEvent event = new GameChangedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId(game.getUserId());
        event.setGameId(game.getId());
        event.setFieldName(fieldName);
        event.setOldValue(oldValue != null ? oldValue.toString() : null);
        event.setNewValue(newValue != null ? newValue.toString() : null);
        event.setOccurredAt(LocalDateTime.now());

        gameEventOutbox.register(event);
    }

    private GameResponseDTO addGameFromCatalog(Long userId, CreateGameDTO dto) {
        CatalogGameDTO catalogGame = catalogServiceClient.getGameByRawgId(dto.getRawgId());

        Game game = Game.builder().userId(userId)
                .rawgId(catalogGame.getRawgId())
                .name(catalogGame.getName())
                .imageUrl(catalogGame.getImageUrl())
                .status(dto.getStatus())
                .rating(dto.getRating()).build();

        Game savedGame = gameRepository.save(game);
        return toResponseDTO(savedGame);
    }

    private Game findGameById(Long userId, Long id) {
        return gameRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new GameNotFoundException("Game not found"));
    }

    private GameResponseDTO toResponseDTO(Game game) {
        GameResponseDTO gameResponseDTO = new GameResponseDTO();

        gameResponseDTO.setId(game.getId());
        gameResponseDTO.setRawgId(game.getRawgId());
        gameResponseDTO.setName(game.getName());
        gameResponseDTO.setImageUrl(game.getImageUrl());
        gameResponseDTO.setStatus(game.getStatus());
        gameResponseDTO.setRating(game.getRating());
        gameResponseDTO.setCreatedAt(game.getCreatedAt());

        return gameResponseDTO;
    }

    private GameHistoryDTO toHistoryDTO(GameHistory history) {
        GameHistoryDTO dto = new GameHistoryDTO();
        dto.setFieldName(history.getFieldName());
        dto.setOldValue(history.getOldValue());
        dto.setNewValue(history.getNewValue());
        dto.setChangedAt(history.getChangedAt());
        return dto;
    }

}
