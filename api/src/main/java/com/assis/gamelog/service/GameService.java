package com.assis.gamelog.service;

import com.assis.gamelog.dto.request.CreateGameDTO;
import com.assis.gamelog.dto.request.UpdateGameDTO;
import com.assis.gamelog.dto.response.GameResponseDTO;

import java.util.List;

public interface GameService {
    GameResponseDTO addGame(CreateGameDTO dto);
    List<GameResponseDTO> getAllGames();
    GameResponseDTO getGameById(Long id);
    GameResponseDTO updateGame(Long id, UpdateGameDTO dto);
    void deleteGame(Long id);
}
