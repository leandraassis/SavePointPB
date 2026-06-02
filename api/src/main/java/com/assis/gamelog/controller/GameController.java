package com.assis.gamelog.controller;

import com.assis.gamelog.dto.rawg.RawgGameDTO;
import com.assis.gamelog.dto.response.SearchGameDTO;
import com.assis.gamelog.service.RawgService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameController {
    private final RawgService rawgService;

    @GetMapping("/test")
    public List<SearchGameDTO> test() {
        return rawgService.searchGames("witcher");
    }

    @GetMapping("/test/{id}")
    public RawgGameDTO test(@PathVariable Long id) {
        return rawgService.getGameById(id);
    }
}
