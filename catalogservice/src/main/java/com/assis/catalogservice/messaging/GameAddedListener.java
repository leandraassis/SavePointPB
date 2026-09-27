package com.assis.catalogservice.messaging;

import com.assis.catalogservice.config.RabbitConfig;
import com.assis.catalogservice.dto.CatalogGameDTO;
import com.assis.catalogservice.dto.event.CatalogGameResolvedEvent;
import com.assis.catalogservice.dto.event.GameAddedEvent;
import com.assis.catalogservice.service.CatalogGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GameAddedListener {

    private final CatalogGameService catalogGameService;
    private final RabbitTemplate rabbitTemplate;

    //reprocessar o mesmo evento é seguro: o jogo já estará em cache e o evento publicado só repete os mesmos dados
    @RabbitListener(queues = RabbitConfig.GAME_ADDED_QUEUE)
    public void onGameAdded(GameAddedEvent event) {
        CatalogGameDTO game = catalogGameService.getGameByRawgId(event.getRawgId());

        CatalogGameResolvedEvent resolvedEvent = new CatalogGameResolvedEvent();
        resolvedEvent.setEventId(UUID.randomUUID().toString());
        resolvedEvent.setRawgId(game.getRawgId());
        resolvedEvent.setName(game.getName());
        resolvedEvent.setImageUrl(game.getImageUrl());

        rabbitTemplate.convertAndSend(RabbitConfig.CATALOG_EVENTS_EXCHANGE, RabbitConfig.GAME_RESOLVED_ROUTING_KEY, resolvedEvent);
    }
}
