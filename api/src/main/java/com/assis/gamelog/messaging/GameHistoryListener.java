package com.assis.gamelog.messaging;

import com.assis.gamelog.config.RabbitConfig;
import com.assis.gamelog.dto.event.GameChangedEvent;
import com.assis.gamelog.model.GameHistory;
import com.assis.gamelog.repository.GameHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameHistoryListener {

    private final GameHistoryRepository gameHistoryRepository;

    @RabbitListener(queues = RabbitConfig.GAME_HISTORY_QUEUE)
    public void onGameChanged(GameChangedEvent event) {

        if(gameHistoryRepository.existsByEventId(event.getEventId())) return;

        GameHistory history = GameHistory.builder()
                .eventId(event.getEventId())
                .userId(event.getUserId())
                .gameId(event.getGameId())
                .fieldName(event.getFieldName())
                .oldValue(event.getOldValue())
                .newValue(event.getNewValue())
                .changedAt(event.getOccurredAt())
                .build();

        gameHistoryRepository.save(history);
    }
}
