package com.assis.gamelog;

import com.assis.gamelog.model.Game;
import com.assis.gamelog.model.GameStatus;
import com.assis.gamelog.repository.GameRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GameRepositoryTest {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistGameAndGenerateId() {
        Game game = Game.builder()
                .rawgId(100L)
                .name("Elden Ring")
                .status(GameStatus.PLAYING)
                .build();

        Game saved = gameRepository.save(game);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void shouldReturnTrueWhenRawgIdExists() {
        Game game = Game.builder()
                .rawgId(200L)
                .name("Hollow Knight")
                .status(GameStatus.COMPLETED)
                .build();

        entityManager.persistAndFlush(game);

        assertTrue(gameRepository.existsByRawgId(200L));
        assertFalse(gameRepository.existsByRawgId(999L));
    }

    @Test
    void shouldNotAllowDuplicateRawgId() {
        Game game1 = Game.builder()
                .rawgId(300L)
                .name("Hades")
                .status(GameStatus.PLAYING)
                .build();
        entityManager.persistAndFlush(game1);

        Game game2 = Game.builder()
                .rawgId(300L)
                .name("Hades(duplicado)")
                .status(GameStatus.WISHLIST)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            gameRepository.saveAndFlush(game2);
        });
    }

    @Test
    void shouldNotAllowNullName() {
        Game game = Game.builder()
                .rawgId(400L)
                .status(GameStatus.PLAYING)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            gameRepository.saveAndFlush(game);
        });
    }

    @Test
    void shouldRespectRatingBoundaries() {
        Game game = Game.builder()
                .rawgId(500L)
                .name("Celeste")
                .status(GameStatus.COMPLETED)
                .rating(6)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            gameRepository.saveAndFlush(game);
        });
    }
}
