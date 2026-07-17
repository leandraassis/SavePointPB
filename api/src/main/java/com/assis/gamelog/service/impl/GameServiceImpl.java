package com.assis.gamelog.service.impl;

import com.assis.gamelog.dto.rawg.RawgGameDTO;
import com.assis.gamelog.dto.request.CreateGameDTO;
import com.assis.gamelog.dto.request.UpdateGameDTO;
import com.assis.gamelog.dto.response.GameHistoryDTO;
import com.assis.gamelog.dto.response.GameResponseDTO;
import com.assis.gamelog.exception.GameAlreadyExistsException;
import com.assis.gamelog.exception.GameNotFoundException;
import com.assis.gamelog.model.Game;
import com.assis.gamelog.model.GameHistory;
import com.assis.gamelog.repository.GameHistoryRepository;
import com.assis.gamelog.repository.GameRepository;
import com.assis.gamelog.service.GameService;
import com.assis.gamelog.service.RawgService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final RawgService rawgService;
    private final GameHistoryRepository gameHistoryRepository;

    @Override
    public GameResponseDTO addGame(CreateGameDTO dto) {
        if(gameRepository.existsByRawgId(dto.getRawgId())) throw new GameAlreadyExistsException("Game already exists");

        RawgGameDTO rawgGame = rawgService.getGameById(dto.getRawgId());

        Game game = Game.builder().rawgId(rawgGame.getId())
                .name(rawgGame.getName())
                .imageUrl(rawgGame.getBackgroundImage())
                .status(dto.getStatus())
                .rating(dto.getRating()).build();

        Game savedGame = gameRepository.save(game);
        return toResponseDTO(savedGame);
    }

    @Override
    public List<GameResponseDTO> getAllGames() {
        return gameRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    @Override
    public GameResponseDTO getGameById(Long id) {
        return toResponseDTO(findGameById(id));
    }

    @Override
    public GameResponseDTO updateGame(Long id, UpdateGameDTO dto) {
        Game game = findGameById(id);
        if(dto.getStatus() != null) {
            logChange(game.getId(), "status", game.getStatus(), dto.getStatus());
            game.setStatus(dto.getStatus());
        }
        if(dto.getRating() != null) {
            logChange(game.getId(), "rating", game.getRating(), dto.getRating());
            game.setRating(dto.getRating());
        }

        Game updatedGame = gameRepository.save(game);
        return toResponseDTO(updatedGame);
    }

    @Override
    @Transactional
    public void deleteGame(Long id) {
        Game game = findGameById(id);
        logChange(game.getId(), "status", game.getStatus(), "DELETED");
        gameRepository.delete(game);
    }

    @Override
    public List<GameHistoryDTO> getGameHistory(Long id) {
        return gameHistoryRepository.findByGameIdOrderByChangedAtDesc(id).stream()
                .map(this::toHistoryDTO)
                .toList();
    }

    //
    private void logChange(Long gameId, String fieldName, Object oldValue, Object newValue) {
        boolean changed = !Objects.equals(oldValue, newValue);

        if (!changed) return;

        GameHistory history = GameHistory.builder()
                .gameId(gameId)
                .fieldName(fieldName)
                .oldValue(oldValue != null ? oldValue.toString() : null)
                .newValue(newValue != null ? newValue.toString() : null)
                .build();

        gameHistoryRepository.save(history);
    }

    private Game findGameById(Long id) {
        return gameRepository.findById(id).orElseThrow(() -> new GameNotFoundException("Game not found"));
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
