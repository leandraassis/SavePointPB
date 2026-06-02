package com.assis.gamelog.service.impl;

import com.assis.gamelog.dto.rawg.RawgGameDTO;
import com.assis.gamelog.dto.request.CreateGameDTO;
import com.assis.gamelog.dto.request.UpdateGameDTO;
import com.assis.gamelog.dto.response.GameResponseDTO;
import com.assis.gamelog.model.Game;
import com.assis.gamelog.repository.GameRepository;
import com.assis.gamelog.service.GameService;
import com.assis.gamelog.service.RawgService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final RawgService rawgService;

    @Override
    public GameResponseDTO addGame(CreateGameDTO dto) {
        if(gameRepository.existsByRawgId(dto.getRawgId())) throw new RuntimeException("Game already exists");

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
        if(dto.getStatus() != null) game.setStatus(dto.getStatus());
        if(dto.getRating() != null) game.setRating(dto.getRating());

        Game updatedGame = gameRepository.save(game);
        return toResponseDTO(updatedGame);
    }

    @Override
    public void deleteGame(Long id) {
        gameRepository.delete(findGameById(id));
    }

    //

    private Game findGameById(Long id) {
        return gameRepository.findById(id).orElseThrow(() -> new RuntimeException("Game not found"));
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

}
