package com.assis.catalogservice.controller;

import com.assis.catalogservice.dto.CatalogGameDTO;
import com.assis.catalogservice.service.CatalogGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogGameController {

    private final CatalogGameService catalogGameService;

    @GetMapping("/search")
    public List<CatalogGameDTO> searchGames(@RequestParam String query) {
        return catalogGameService.searchGames(query);
    }

    @GetMapping("/{rawgId}")
    public CatalogGameDTO getGameByRawgId(@PathVariable Long rawgId) {
        return catalogGameService.getGameByRawgId(rawgId);
    }
}
