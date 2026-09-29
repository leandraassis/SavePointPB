package com.assis.gamelog;

import com.assis.gamelog.client.CatalogServiceClient;
import com.assis.gamelog.config.SecurityConfig;
import com.assis.gamelog.controller.GameController;
import com.assis.gamelog.dto.catalog.CatalogGameDTO;
import com.assis.gamelog.dto.request.CreateGameDTO;
import com.assis.gamelog.dto.request.UpdateGameDTO;
import com.assis.gamelog.dto.response.GameHistoryDTO;
import com.assis.gamelog.dto.response.GameResponseDTO;
import com.assis.gamelog.exception.GameAlreadyExistsException;
import com.assis.gamelog.exception.GameNotFoundException;
import com.assis.gamelog.model.GameStatus;
import com.assis.gamelog.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "jwt.secret=test-secret-with-at-least-32-bytes!!")
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @MockitoBean
    private CatalogServiceClient catalogServiceClient;

    @Test
    void shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(gameService);
    }

    @Test
    void shouldListGamesOfAuthenticatedUser() throws Exception {
        when(gameService.getAllGames(1L)).thenReturn(List.of(gameResponse()));

        mockMvc.perform(get("/api/games").with(user(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Elden Ring"))
                .andExpect(jsonPath("$[0].status").value("PLAYING"));
    }

    @Test
    void shouldAddGameForUserFromToken() throws Exception {
        when(gameService.addGame(eq(1L), any(CreateGameDTO.class))).thenReturn(gameResponse());

        mockMvc.perform(post("/api/games").with(user(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rawgId": 100, "status": "PLAYING", "name": "Elden Ring"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawgId").value(100));
    }

    @Test
    void shouldReturn400WhenCreateGameIsInvalid() throws Exception {
        mockMvc.perform(post("/api/games").with(user(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PLAYING", "rating": 6}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameService);
    }

    @Test
    void shouldReturn409WhenGameAlreadyExists() throws Exception {
        when(gameService.addGame(eq(1L), any(CreateGameDTO.class))).thenThrow(new GameAlreadyExistsException("Game already exists"));

        mockMvc.perform(post("/api/games").with(user(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rawgId": 100, "status": "PLAYING"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().string("Game already exists"));
    }

    @Test
    void shouldReturn404WhenGameIsNotFound() throws Exception {
        when(gameService.getGameById(1L, 99L)).thenThrow(new GameNotFoundException("Game not found"));

        mockMvc.perform(get("/api/games/99").with(user(1L)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Game not found"));
    }

    @Test
    void shouldReturnGameById() throws Exception {
        when(gameService.getGameById(1L, 10L)).thenReturn(gameResponse());

        mockMvc.perform(get("/api/games/10").with(user(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void shouldUpdateGame() throws Exception {
        GameResponseDTO updated = gameResponse();
        updated.setStatus(GameStatus.COMPLETED);
        updated.setRating(5);
        when(gameService.updateGame(eq(1L), eq(10L), any(UpdateGameDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/games/10").with(user(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "COMPLETED", "rating": 5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void shouldReturn400WhenRatingIsOutOfRangeOnUpdate() throws Exception {
        mockMvc.perform(put("/api/games/10").with(user(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rating": 0}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameService);
    }

    @Test
    void shouldDeleteGameAndReturn204() throws Exception {
        mockMvc.perform(delete("/api/games/10").with(user(1L)))
                .andExpect(status().isNoContent());

        verify(gameService).deleteGame(1L, 10L);
    }

    @Test
    void shouldReturnGameHistory() throws Exception {
        GameHistoryDTO history = new GameHistoryDTO();
        history.setFieldName("status");
        history.setOldValue("PLAYING");
        history.setNewValue("COMPLETED");
        when(gameService.getGameHistory(1L, 10L)).thenReturn(List.of(history));

        mockMvc.perform(get("/api/games/10/history").with(user(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].newValue").value("COMPLETED"));
    }

    @Test
    void shouldSearchGamesInCatalog() throws Exception {
        CatalogGameDTO catalogGame = new CatalogGameDTO();
        catalogGame.setRawgId(100L);
        catalogGame.setName("Elden Ring");
        when(catalogServiceClient.searchGames("elden")).thenReturn(List.of(catalogGame));

        mockMvc.perform(get("/api/games/search").param("query", "elden").with(user(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Elden Ring"));
    }

    private RequestPostProcessor user(Long userId) {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }

    private GameResponseDTO gameResponse() {
        GameResponseDTO dto = new GameResponseDTO();
        dto.setId(10L);
        dto.setRawgId(100L);
        dto.setName("Elden Ring");
        dto.setStatus(GameStatus.PLAYING);
        return dto;
    }
}
