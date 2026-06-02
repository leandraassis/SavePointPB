package com.assis.gamelog.service;

import com.assis.gamelog.dto.rawg.RawgGameDTO;
import com.assis.gamelog.dto.response.SearchGameDTO;

import java.util.List;

public interface RawgService {

    List<SearchGameDTO> searchGames(String gameName);
    RawgGameDTO getGameById(Long rawgId);
}
