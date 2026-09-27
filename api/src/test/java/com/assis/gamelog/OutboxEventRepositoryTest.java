package com.assis.gamelog;

import com.assis.gamelog.model.OutboxEvent;
import com.assis.gamelog.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OutboxEventRepositoryTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistOutboxEventAndGenerateCreatedAt() {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId("event-1")
                .routingKey("game.status.changed")
                .payload("{}")
                .build();

        OutboxEvent saved = outboxEventRepository.save(outboxEvent);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNull(saved.getPublishedAt());
    }

    @Test
    void shouldReturnOnlyPendingEventsOrderedByCreation() {
        entityManager.persistAndFlush(OutboxEvent.builder()
                .eventId("second").routingKey("game.status.changed").payload("{}")
                .createdAt(LocalDateTime.now().minusMinutes(1))
                .build());
        entityManager.persistAndFlush(OutboxEvent.builder()
                .eventId("first").routingKey("game.rating.changed").payload("{}")
                .createdAt(LocalDateTime.now().minusMinutes(2))
                .build());
        entityManager.persistAndFlush(OutboxEvent.builder()
                .eventId("published").routingKey("game.deleted").payload("{}")
                .publishedAt(LocalDateTime.now())
                .build());

        List<OutboxEvent> result = outboxEventRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();

        assertEquals(2, result.size());
        assertEquals("first", result.get(0).getEventId());
        assertEquals("second", result.get(1).getEventId());
    }

    @Test
    void shouldNotAllowDuplicateEventId() {
        entityManager.persistAndFlush(OutboxEvent.builder()
                .eventId("same").routingKey("game.status.changed").payload("{}")
                .build());

        OutboxEvent duplicated = OutboxEvent.builder()
                .eventId("same").routingKey("game.status.changed").payload("{}")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            outboxEventRepository.saveAndFlush(duplicated);
        });
    }
}
