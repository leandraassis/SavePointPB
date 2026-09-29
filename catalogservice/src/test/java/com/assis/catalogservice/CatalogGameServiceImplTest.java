package com.assis.catalogservice;

import com.assis.catalogservice.dto.CatalogGameDTO;
import com.assis.catalogservice.dto.rawg.RawgGameDTO;
import com.assis.catalogservice.model.CatalogGame;
import com.assis.catalogservice.repository.CatalogGameRepository;
import com.assis.catalogservice.service.RawgService;
import com.assis.catalogservice.service.impl.CatalogGameServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogGameServiceImplTest {

    @Mock
    private CatalogGameRepository catalogGameRepository;

    @Mock
    private RawgService rawgService;

    @InjectMocks
    private CatalogGameServiceImpl catalogGameService;

    @Test
    void shouldSearchGamesInRawg() {
        CatalogGameDTO game = new CatalogGameDTO();
        game.setRawgId(100L);
        game.setName("Elden Ring");
        when(rawgService.searchGames("elden")).thenReturn(List.of(game));

        List<CatalogGameDTO> results = catalogGameService.searchGames("elden");

        assertEquals(List.of(game), results);
        verifyNoInteractions(catalogGameRepository);
    }

    @Test
    void shouldReturnCachedGameWithoutCallingRawg() {
        CatalogGame cached = CatalogGame.builder().rawgId(100L).name("Elden Ring").imageUrl("https://img/elden.jpg").build();
        when(catalogGameRepository.findByRawgId(100L)).thenReturn(Optional.of(cached));

        CatalogGameDTO dto = catalogGameService.getGameByRawgId(100L);

        assertEquals("Elden Ring", dto.getName());
        verify(rawgService, never()).getGameById(any());
    }

    @Test
    void shouldFetchFromRawgAndCacheWhenGameIsNotCached() {
        RawgGameDTO rawgGame = new RawgGameDTO();
        rawgGame.setId(100L);
        rawgGame.setName("Elden Ring");
        rawgGame.setBackgroundImage("https://img/elden.jpg");
        when(catalogGameRepository.findByRawgId(100L)).thenReturn(Optional.empty());
        when(rawgService.getGameById(100L)).thenReturn(rawgGame);
        when(catalogGameRepository.save(any(CatalogGame.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CatalogGameDTO dto = catalogGameService.getGameByRawgId(100L);

        assertEquals(100L, dto.getRawgId());
        assertEquals("https://img/elden.jpg", dto.getImageUrl());
        ArgumentCaptor<CatalogGame> captor = ArgumentCaptor.forClass(CatalogGame.class);
        verify(catalogGameRepository).save(captor.capture());
        assertEquals("Elden Ring", captor.getValue().getName());
    }
}
