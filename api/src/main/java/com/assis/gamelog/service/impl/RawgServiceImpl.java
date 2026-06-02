package com.assis.gamelog.service.impl;

import com.assis.gamelog.dto.rawg.RawgGameDTO;
import com.assis.gamelog.dto.rawg.RawgSearchResponseDTO;
import com.assis.gamelog.dto.response.SearchGameDTO;
import com.assis.gamelog.service.RawgService;
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
    public List<SearchGameDTO> searchGames(String gameName) {
        RawgSearchResponseDTO response = rawgRestClient.get().uri(uriBuilder -> uriBuilder
                .path("/games")
                .queryParam("key", apiKey)
                .queryParam("search", gameName)
                .build()).retrieve().body(RawgSearchResponseDTO.class);

        return response.getResults().stream().map(this::toSearchGameDTO).toList();
    }

    @Override
    public RawgGameDTO getGameById(Long rawgId) {
        return rawgRestClient.get().uri(uriBuilder -> uriBuilder
                .path("/games/{id}")
                .queryParam("key", apiKey)
                .build(rawgId)).retrieve().body(RawgGameDTO.class);
    }

    private SearchGameDTO toSearchGameDTO(RawgGameDTO rawgGame) {
        SearchGameDTO dto = new SearchGameDTO();

        dto.setRawgId(rawgGame.getId());
        dto.setName(rawgGame.getName());
        dto.setImageUrl(rawgGame.getBackgroundImage());
        return dto;
    }

}
