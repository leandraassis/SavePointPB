package com.assis.catalogservice.service.impl;

import com.assis.catalogservice.dto.CatalogGameDTO;
import com.assis.catalogservice.dto.rawg.RawgGameDTO;
import com.assis.catalogservice.model.CatalogGame;
import com.assis.catalogservice.repository.CatalogGameRepository;
import com.assis.catalogservice.service.CatalogGameService;
import com.assis.catalogservice.service.RawgService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogGameServiceImpl implements CatalogGameService {

    private final CatalogGameRepository catalogGameRepository;
    private final RawgService rawgService;

    @Override
    public List<CatalogGameDTO> searchGames(String query) {
        return rawgService.searchGames(query);
    }

    @Override
    public CatalogGameDTO getGameByRawgId(Long rawgId) {
        CatalogGame game = catalogGameRepository.findByRawgId(rawgId).orElseGet(() -> fetchAndCache(rawgId));
        return toDTO(game);
    }

    private CatalogGame fetchAndCache(Long rawgId) {
        RawgGameDTO rawgGame = rawgService.getGameById(rawgId);

        CatalogGame game = CatalogGame.builder()
                .rawgId(rawgGame.getId())
                .name(rawgGame.getName())
                .imageUrl(rawgGame.getBackgroundImage())
                .build();

        return catalogGameRepository.save(game);
    }

    private CatalogGameDTO toDTO(CatalogGame game) {
        CatalogGameDTO dto = new CatalogGameDTO();
        dto.setRawgId(game.getRawgId());
        dto.setName(game.getName());
        dto.setImageUrl(game.getImageUrl());
        return dto;
    }
}