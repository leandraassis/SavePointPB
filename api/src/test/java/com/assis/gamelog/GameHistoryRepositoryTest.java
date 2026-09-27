package com.assis.gamelog;

import com.assis.gamelog.model.Game;
import com.assis.gamelog.model.GameHistory;
import com.assis.gamelog.model.GameStatus;
import com.assis.gamelog.repository.GameHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GameHistoryRepositoryTest {

    @Autowired
    private GameHistoryRepository historyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistHistoryEntryAndGenerateChangedAt() {
        GameHistory history = GameHistory.builder()
                .userId(1L)
                .gameId(1L)
                .fieldName("status")
                .oldValue("PLAYING")
                .newValue("COMPLETED")
                .build();

        GameHistory saved = historyRepository.save(history);

        assertNotNull(saved.getId());
        assertNotNull(saved.getChangedAt());
    }

    @Test
    void shouldFindHistoryByGameIdOrderedByMostRecent() throws InterruptedException {
        GameHistory first = GameHistory.builder()
                .userId(1L).gameId(10L).fieldName("status").oldValue("PLAYING").newValue("DROPPED")
                .build();
        entityManager.persistAndFlush(first);

        Thread.sleep(10); //pro changedAt ser diferente entre os registros

        GameHistory second = GameHistory.builder()
                .userId(1L).gameId(10L).fieldName("rating").oldValue(null).newValue("4")
                .build();
        entityManager.persistAndFlush(second);

        List<GameHistory> result = historyRepository.findByUserIdAndGameIdOrderByChangedAtDesc(1L, 10L);

        assertEquals(2, result.size());
        assertEquals("rating", result.get(0).getFieldName());
        assertEquals("status", result.get(1).getFieldName());
    }

    @Test
    void shouldReturnEmptyListWhenNoHistoryExistsForGameId() {
        List<GameHistory> result = historyRepository.findByUserIdAndGameIdOrderByChangedAtDesc(1L, 999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotReturnHistoryFromAnotherUser() {
        GameHistory history = GameHistory.builder()
                .userId(1L).gameId(20L).fieldName("status").oldValue("PLAYING").newValue("COMPLETED")
                .build();
        entityManager.persistAndFlush(history);

        List<GameHistory> result = historyRepository.findByUserIdAndGameIdOrderByChangedAtDesc(2L, 20L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldPersistHistoryEvenAfterGameIsDeleted() {
        Game game = Game.builder()
                .userId(1L).rawgId(600L).name("Hollow Knight").status(GameStatus.COMPLETED)
                .build();
        entityManager.persistAndFlush(game);

        GameHistory history = GameHistory.builder()
                .userId(1L).gameId(game.getId()).fieldName("status").oldValue("COMPLETED").newValue("DELETED")
                .build();
        entityManager.persistAndFlush(history);

        entityManager.remove(game);
        entityManager.flush();

        List<GameHistory> result = historyRepository.findByUserIdAndGameIdOrderByChangedAtDesc(1L, game.getId());

        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnTrueWhenEventIdAlreadyExists() {
        GameHistory history = GameHistory.builder()
                .eventId("event-1").userId(1L).gameId(30L).fieldName("status").oldValue("PLAYING").newValue("COMPLETED")
                .build();
        entityManager.persistAndFlush(history);

        assertTrue(historyRepository.existsByEventId("event-1"));
        assertFalse(historyRepository.existsByEventId("event-2"));
    }

    @Test
    void shouldNotAllowDuplicateEventId() {
        GameHistory first = GameHistory.builder()
                .eventId("event-3").userId(1L).gameId(40L).fieldName("status").oldValue("PLAYING").newValue("COMPLETED")
                .build();
        entityManager.persistAndFlush(first);

        GameHistory duplicated = GameHistory.builder()
                .eventId("event-3").userId(1L).gameId(40L).fieldName("status").oldValue("PLAYING").newValue("COMPLETED")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            historyRepository.saveAndFlush(duplicated);
        });
    }
}
