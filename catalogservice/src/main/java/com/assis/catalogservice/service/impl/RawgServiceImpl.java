package com.assis.catalogservice.service.impl;

import com.assis.catalogservice.dto.CatalogGameDTO;
import com.assis.catalogservice.dto.rawg.RawgGameDTO;
import com.assis.catalogservice.dto.rawg.RawgSearchResponseDTO;
import com.assis.catalogservice.service.RawgService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RawgServiceImpl implements RawgService {
    private final RestClient rawgRestClient;

    @Value("${rawg.api.key}")
    private String apiKey;

    @Override
    public List<CatalogGameDTO> searchGames(String gameName) {
        RawgSearchResponseDTO response = rawgRestClient.get().uri(uriBuilder -> uriBuilder
                .path("/games")
                .queryParam("key", apiKey)
                .queryParam("search", gameName)
                .build()).retrieve().body(RawgSearchResponseDTO.class);

        return response.getResults().stream().map(this::toCatalogGameDTO).toList();
    }

    @Override
    public RawgGameDTO getGameById(Long rawgId) {
        return rawgRestClient.get().uri(uriBuilder -> uriBuilder
                .path("/games/{id}")
                .queryParam("key", apiKey)
                .build(rawgId)).retrieve().body(RawgGameDTO.class);
    }

    private CatalogGameDTO toCatalogGameDTO(RawgGameDTO rawgGame) {
        CatalogGameDTO dto = new CatalogGameDTO();

        dto.setRawgId(rawgGame.getId());
        dto.setName(rawgGame.getName());
        dto.setImageUrl(rawgGame.getBackgroundImage());
        return dto;
    }

}
