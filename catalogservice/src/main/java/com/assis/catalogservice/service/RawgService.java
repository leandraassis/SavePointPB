package com.assis.catalogservice.service;

import com.assis.catalogservice.dto.CatalogGameDTO;
import com.assis.catalogservice.dto.rawg.RawgGameDTO;

import java.util.List;

public interface RawgService {
    List<CatalogGameDTO> searchGames(String gameName);
    RawgGameDTO getGameById(Long rawgId);
}