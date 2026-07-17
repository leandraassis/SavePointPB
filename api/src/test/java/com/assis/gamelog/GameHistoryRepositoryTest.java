package com.assis.gamelog;

import com.assis.gamelog.model.Game;
import com.assis.gamelog.model.GameHistory;
import com.assis.gamelog.model.GameStatus;
import com.assis.gamelog.repository.GameHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

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
                .gameId(10L).fieldName("status").oldValue("PLAYING").newValue("DROPPED")
                .build();
        entityManager.persistAndFlush(first);

        Thread.sleep(10); //pro changedAt ser diferente entre os registros

        GameHistory second = GameHistory.builder()
                .gameId(10L).fieldName("rating").oldValue(null).newValue("4")
                .build();
        entityManager.persistAndFlush(second);

        List<GameHistory> result = historyRepository.findByGameIdOrderByChangedAtDesc(10L);

        assertEquals(2, result.size());
        assertEquals("rating", result.get(0).getFieldName());
        assertEquals("status", result.get(1).getFieldName());
    }

    @Test
    void shouldReturnEmptyListWhenNoHistoryExistsForGameId() {
        List<GameHistory> result = historyRepository.findByGameIdOrderByChangedAtDesc(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldPersistHistoryEvenAfterGameIsDeleted() {
        Game game = Game.builder()
                .rawgId(600L).name("Hollow Knight").status(GameStatus.COMPLETED)
                .build();
        entityManager.persistAndFlush(game);

        GameHistory history = GameHistory.builder()
                .gameId(game.getId()).fieldName("status").oldValue("COMPLETED").newValue("DELETED")
                .build();
        entityManager.persistAndFlush(history);

        entityManager.remove(game);
        entityManager.flush();

        List<GameHistory> result = historyRepository.findByGameIdOrderByChangedAtDesc(game.getId());

        assertEquals(1, result.size());
    }
}
