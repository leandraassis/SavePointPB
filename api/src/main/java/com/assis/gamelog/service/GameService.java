package com.assis.gamelog.service;

import com.assis.gamelog.dto.request.CreateGameDTO;
import com.assis.gamelog.dto.request.UpdateGameDTO;
import com.assis.gamelog.dto.response.GameHistoryDTO;
import com.assis.gamelog.dto.response.GameResponseDTO;

import java.util.List;

public interface GameService {
    GameResponseDTO addGame(Long userId, CreateGameDTO dto);
    List<GameResponseDTO> getAllGames(Long userId);
    GameResponseDTO getGameById(Long userId, Long id);
    GameResponseDTO updateGame(Long userId, Long id, UpdateGameDTO dto);
    void deleteGame(Long userId, Long id);
    List<GameHistoryDTO> getGameHistory(Long userId, Long id);
}
