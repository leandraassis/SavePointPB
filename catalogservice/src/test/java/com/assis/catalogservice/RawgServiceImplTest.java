package com.assis.catalogservice;

import com.assis.catalogservice.dto.CatalogGameDTO;
import com.assis.catalogservice.dto.rawg.RawgGameDTO;
import com.assis.catalogservice.service.impl.RawgServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RawgServiceImplTest {

    private MockRestServiceServer server;
    private RawgServiceImpl rawgService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://rawg.test/api");
        server = MockRestServiceServer.bindTo(builder).build();
        rawgService = new RawgServiceImpl(builder.build());
        ReflectionTestUtils.setField(rawgService, "apiKey", "test-key");
    }

    @Test
    void shouldSearchGamesAndMapResults() {
        server.expect(requestTo("https://rawg.test/api/games?key=test-key&search=elden"))
                .andRespond(withSuccess("""
                        {"results": [{"id": 100, "name": "Elden Ring", "background_image": "https://img/elden.jpg", "rating": 4.5}]}
                        """, MediaType.APPLICATION_JSON));

        List<CatalogGameDTO> results = rawgService.searchGames("elden");

        assertEquals(1, results.size());
        assertEquals(100L, results.get(0).getRawgId());
        assertEquals("Elden Ring", results.get(0).getName());
        assertEquals("https://img/elden.jpg", results.get(0).getImageUrl());
        server.verify();
    }

    @Test
    void shouldGetGameById() {
        server.expect(requestTo("https://rawg.test/api/games/100?key=test-key"))
                .andRespond(withSuccess("""
                        {"id": 100, "name": "Elden Ring", "background_image": "https://img/elden.jpg"}
                        """, MediaType.APPLICATION_JSON));

        RawgGameDTO game = rawgService.getGameById(100L);

        assertEquals("Elden Ring", game.getName());
        assertEquals("https://img/elden.jpg", game.getBackgroundImage());
    }

    @Test
    void shouldPropagateErrorWhenApiKeyIsInvalid() {
        server.expect(requestTo("https://rawg.test/api/games?key=test-key&search=elden"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThrows(HttpClientErrorException.Unauthorized.class, () -> rawgService.searchGames("elden"));
    }
}
