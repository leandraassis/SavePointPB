package com.assis.gamelog.controller;

import com.assis.gamelog.dto.catalog.CatalogGameDTO;
import com.assis.gamelog.dto.request.CreateGameDTO;
import com.assis.gamelog.dto.request.UpdateGameDTO;
import com.assis.gamelog.dto.response.GameHistoryDTO;
import com.assis.gamelog.dto.response.GameResponseDTO;
import com.assis.gamelog.client.CatalogServiceClient;
import com.assis.gamelog.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final CatalogServiceClient catalogServiceClient;

    @GetMapping
    public List<GameResponseDTO> getAllGames() {
        return gameService.getAllGames();
    }

    @PostMapping
    public GameResponseDTO addGame(@RequestBody @Valid CreateGameDTO dto) {
        return gameService.addGame(dto);
    }

    @GetMapping("/{id}")
    public GameResponseDTO getGameById(@PathVariable Long id) {
        return gameService.getGameById(id);
    }

    @PutMapping("/{id}")
    public GameResponseDTO updateGame(@PathVariable Long id, @RequestBody @Valid UpdateGameDTO dto) {
        return gameService.updateGame(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
    }

    @GetMapping("/search")
    public List<CatalogGameDTO> searchGames(@RequestParam String query) {
        return catalogServiceClient.searchGames(query);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<GameHistoryDTO>> getGameHistory(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGameHistory(id));
    }

}
