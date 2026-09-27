package com.assis.gamelog.messaging;

import com.assis.gamelog.config.RabbitConfig;
import com.assis.gamelog.dto.event.CatalogGameResolvedEvent;
import com.assis.gamelog.model.Game;
import com.assis.gamelog.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogGameListener {

    private final GameRepository gameRepository;

    @RabbitListener(queues = RabbitConfig.CATALOG_GAME_RESOLVED_QUEUE)
    public void onCatalogGameResolved(CatalogGameResolvedEvent event) {
        List<Game> games = gameRepository.findByRawgId(event.getRawgId());

        games.forEach(game -> {
            game.setName(event.getName());
            game.setImageUrl(event.getImageUrl());
        });

        gameRepository.saveAll(games);
    }
}
