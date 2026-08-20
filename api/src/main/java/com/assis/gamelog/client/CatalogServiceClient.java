package com.assis.gamelog.client;

import com.assis.gamelog.dto.catalog.CatalogGameDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "catalogservice")
public interface CatalogServiceClient {

    @GetMapping("/api/catalog/search")
    List<CatalogGameDTO> searchGames(@RequestParam("query") String query);

    @GetMapping("/api/catalog/{rawgId}")
    CatalogGameDTO getGameByRawgId(@PathVariable("rawgId") Long rawgId);
}
