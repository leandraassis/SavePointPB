package com.assis.gamelog;

import com.assis.gamelog.config.RabbitConfig;
import com.assis.gamelog.dto.event.CatalogGameResolvedEvent;
import com.assis.gamelog.dto.event.GameChangedEvent;
import com.assis.gamelog.dto.request.UpdateGameDTO;
import com.assis.gamelog.model.Game;
import com.assis.gamelog.model.GameHistory;
import com.assis.gamelog.model.GameStatus;
import com.assis.gamelog.repository.GameHistoryRepository;
import com.assis.gamelog.repository.GameRepository;
import com.assis.gamelog.repository.OutboxEventRepository;
import com.assis.gamelog.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:events-it",
        "eureka.client.enabled=false",
        "jwt.secret=test-secret-with-at-least-32-bytes!!"
})
class GameEventsIntegrationTest {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-management");

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameHistoryRepository gameHistoryRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        gameHistoryRepository.deleteAll();
        outboxEventRepository.deleteAll();
        gameRepository.deleteAll();
    }

    @Test
    void shouldSaveHistoryAfterUpdatePassesThroughOutboxAndBroker() {
        Game game = saveGame(1L, "Elden Ring");

        UpdateGameDTO dto = new UpdateGameDTO();
        dto.setStatus(GameStatus.COMPLETED);
        gameService.updateGame(1L, game.getId(), dto);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            List<GameHistory> history = gameHistoryRepository.findByUserIdAndGameIdOrderByChangedAtDesc(1L, game.getId());
            assertEquals(1, history.size());
            assertEquals("PLAYING", history.get(0).getOldValue());
            assertEquals("COMPLETED", history.get(0).getNewValue());
        });
        assertTrue(outboxEventRepository.findAll().stream().allMatch(event -> event.getPublishedAt() != null));
    }

    @Test
    void shouldSaveHistoryOnlyOnceWhenEventIsDeliveredTwice() {
        GameChangedEvent event = changedEvent(1L);
        rabbitTemplate.convertAndSend(RabbitConfig.GAME_EVENTS_EXCHANGE, "game.status.changed", event);
        rabbitTemplate.convertAndSend(RabbitConfig.GAME_EVENTS_EXCHANGE, "game.status.changed", event);

        await().atMost(Duration.ofSeconds(10))
                .until(() -> gameHistoryRepository.existsByEventId(event.getEventId()));
        await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(5))
                .until(() -> gameHistoryRepository.count() == 1);
    }

    @Test
    void shouldSendMessageToDeadLetterQueueAfterRetries() {
        GameChangedEvent invalidEvent = changedEvent(null);
        rabbitTemplate.convertAndSend(RabbitConfig.GAME_EVENTS_EXCHANGE, "game.status.changed", invalidEvent);

        Object deadLetter = rabbitTemplate.receiveAndConvert(RabbitConfig.GAME_HISTORY_DLQ, 15000);

        assertInstanceOf(GameChangedEvent.class, deadLetter);
        assertEquals(invalidEvent.getEventId(), ((GameChangedEvent) deadLetter).getEventId());
        assertFalse(gameHistoryRepository.existsByEventId(invalidEvent.getEventId()));
    }

    @Test
    void shouldUpdateGamesWhenCatalogResolvesGame() {
        Game game = saveGame(1L, "elden");

        CatalogGameResolvedEvent event = new CatalogGameResolvedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setRawgId(100L);
        event.setName("Elden Ring");
        event.setImageUrl("https://img/elden.jpg");
        rabbitTemplate.convertAndSend(RabbitConfig.CATALOG_EVENTS_EXCHANGE, "catalog.game.resolved", event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Game updated = gameRepository.findById(game.getId()).orElseThrow();
            assertEquals("Elden Ring", updated.getName());
            assertEquals("https://img/elden.jpg", updated.getImageUrl());
        });
    }

    private Game saveGame(Long userId, String name) {
        return gameRepository.save(Game.builder()
                .userId(userId)
                .rawgId(100L)
                .name(name)
                .status(GameStatus.PLAYING)
                .build());
    }

    private GameChangedEvent changedEvent(Long userId) {
        GameChangedEvent event = new GameChangedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setUserId(userId);
        event.setGameId(10L);
        event.setFieldName("status");
        event.setOldValue("PLAYING");
        event.setNewValue("COMPLETED");
        event.setOccurredAt(LocalDateTime.now());
        return event;
    }
}
