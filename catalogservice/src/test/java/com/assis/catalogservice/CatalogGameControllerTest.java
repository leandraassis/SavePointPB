package com.assis.catalogservice;

import com.assis.catalogservice.controller.CatalogGameController;
import com.assis.catalogservice.dto.CatalogGameDTO;
import com.assis.catalogservice.service.CatalogGameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogGameController.class)
class CatalogGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogGameService catalogGameService;

    @Test
    void shouldSearchGames() throws Exception {
        when(catalogGameService.searchGames("elden")).thenReturn(List.of(catalogGame()));

        mockMvc.perform(get("/api/catalog/search").param("query", "elden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rawgId").value(100))
                .andExpect(jsonPath("$[0].name").value("Elden Ring"));
    }

    @Test
    void shouldReturn400WhenQueryIsMissing() throws Exception {
        mockMvc.perform(get("/api/catalog/search"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(catalogGameService);
    }

    @Test
    void shouldGetGameByRawgId() throws Exception {
        when(catalogGameService.getGameByRawgId(100L)).thenReturn(catalogGame());

        mockMvc.perform(get("/api/catalog/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://img/elden.jpg"));
    }

    @Test
    void shouldReturn400WhenRawgIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/catalog/abc"))
                .andExpect(status().isBadRequest());
    }

    private CatalogGameDTO catalogGame() {
        CatalogGameDTO dto = new CatalogGameDTO();
        dto.setRawgId(100L);
        dto.setName("Elden Ring");
        dto.setImageUrl("https://img/elden.jpg");
        return dto;
    }
}
