package com.assis.gamelog.messaging;

import com.assis.gamelog.config.RabbitConfig;
import com.assis.gamelog.model.OutboxEvent;
import com.assis.gamelog.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {
        for(OutboxEvent outboxEvent : outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            send(outboxEvent);

            outboxEvent.setPublishedAt(LocalDateTime.now());
            outboxEventRepository.save(outboxEvent);
        }
    }

    private void send(OutboxEvent outboxEvent) {
        Message message = MessageBuilder.withBody(outboxEvent.getPayload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setMessageId(outboxEvent.getEventId())
                .build();

        rabbitTemplate.invoke(operations -> {
            operations.send(RabbitConfig.GAME_EVENTS_EXCHANGE, outboxEvent.getRoutingKey(), message);
            operations.waitForConfirmsOrDie(5000);
            return true;
        });
    }
}
