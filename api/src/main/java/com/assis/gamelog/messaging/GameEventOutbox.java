package com.assis.gamelog.messaging;

import com.assis.gamelog.dto.event.GameAddedEvent;
import com.assis.gamelog.dto.event.GameChangedEvent;
import com.assis.gamelog.model.OutboxEvent;
import com.assis.gamelog.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class GameEventOutbox {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    public void register(GameChangedEvent event) {
        save(event.getEventId(), routingKey(event), event);
    }

    public void register(GameAddedEvent event) {
        save(event.getEventId(), "game.added", event);
    }

    private void save(String eventId, String routingKey, Object event) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(eventId)
                .routingKey(routingKey)
                .payload(jsonMapper.writeValueAsString(event))
                .build();

        outboxEventRepository.save(outboxEvent);
    }

    private String routingKey(GameChangedEvent event) {
        if("DELETED".equals(event.getNewValue())) return "game.deleted";

        return "game." + event.getFieldName() + ".changed";
    }
}
