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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final CatalogServiceClient catalogServiceClient;

    @GetMapping
    public List<GameResponseDTO> getAllGames(@AuthenticationPrincipal Jwt jwt) {
        return gameService.getAllGames(getUserId(jwt));
    }

    @PostMapping
    public GameResponseDTO addGame(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid CreateGameDTO dto) {
        return gameService.addGame(getUserId(jwt), dto);
    }

    @GetMapping("/{id}")
    public GameResponseDTO getGameById(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return gameService.getGameById(getUserId(jwt), id);
    }

    @PutMapping("/{id}")
    public GameResponseDTO updateGame(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody @Valid UpdateGameDTO dto) {
        return gameService.updateGame(getUserId(jwt), id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        gameService.deleteGame(getUserId(jwt), id);
    }

    @GetMapping("/search")
    public List<CatalogGameDTO> searchGames(@RequestParam String query) {
        return catalogServiceClient.searchGames(query);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<GameHistoryDTO>> getGameHistory(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGameHistory(getUserId(jwt), id));
    }

    private Long getUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

}
