package com.assis.catalogservice.service;

import com.assis.catalogservice.dto.CatalogGameDTO;

import java.util.List;

public interface CatalogGameService {
    List<CatalogGameDTO> searchGames(String query);
    CatalogGameDTO getGameByRawgId(Long rawgId);
}
