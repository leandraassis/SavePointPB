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

import java.util.List;

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
                .userId(1L)
                .rawgId(100L)
                .name("Elden Ring")
                .status(GameStatus.PLAYING)
                .build();

        Game saved = gameRepository.save(game);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void shouldReturnTrueWhenRawgIdExistsForUser() {
        Game game = Game.builder()
                .userId(1L)
                .rawgId(200L)
                .name("Hollow Knight")
                .status(GameStatus.COMPLETED)
                .build();

        entityManager.persistAndFlush(game);

        assertTrue(gameRepository.existsByUserIdAndRawgId(1L, 200L));
        assertFalse(gameRepository.existsByUserIdAndRawgId(2L, 200L));
        assertFalse(gameRepository.existsByUserIdAndRawgId(1L, 999L));
    }

    @Test
    void shouldNotAllowDuplicateRawgIdForSameUser() {
        Game game1 = Game.builder()
                .userId(1L)
                .rawgId(300L)
                .name("Hades")
                .status(GameStatus.PLAYING)
                .build();
        entityManager.persistAndFlush(game1);

        Game game2 = Game.builder()
                .userId(1L)
                .rawgId(300L)
                .name("Hades(duplicado)")
                .status(GameStatus.WISHLIST)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            gameRepository.saveAndFlush(game2);
        });
    }

    @Test
    void shouldAllowSameRawgIdForDifferentUsers() {
        Game game1 = Game.builder()
                .userId(1L)
                .rawgId(310L)
                .name("Hades")
                .status(GameStatus.PLAYING)
                .build();
        entityManager.persistAndFlush(game1);

        Game game2 = Game.builder()
                .userId(2L)
                .rawgId(310L)
                .name("Hades")
                .status(GameStatus.WISHLIST)
                .build();

        assertDoesNotThrow(() -> gameRepository.saveAndFlush(game2));
    }

    @Test
    void shouldReturnOnlyGamesFromUser() {
        entityManager.persistAndFlush(Game.builder()
                .userId(1L).rawgId(320L).name("Celeste").status(GameStatus.COMPLETED)
                .build());
        entityManager.persistAndFlush(Game.builder()
                .userId(2L).rawgId(330L).name("Hades").status(GameStatus.PLAYING)
                .build());

        List<Game> result = gameRepository.findByUserId(1L);

        assertEquals(1, result.size());
        assertEquals("Celeste", result.get(0).getName());
    }

    @Test
    void shouldNotFindGameFromAnotherUser() {
        Game game = Game.builder()
                .userId(1L)
                .rawgId(340L)
                .name("Celeste")
                .status(GameStatus.COMPLETED)
                .build();
        entityManager.persistAndFlush(game);

        assertTrue(gameRepository.findByIdAndUserId(game.getId(), 1L).isPresent());
        assertTrue(gameRepository.findByIdAndUserId(game.getId(), 2L).isEmpty());
    }

    @Test
    void shouldFindGamesFromAllUsersByRawgId() {
        entityManager.persistAndFlush(Game.builder()
                .userId(1L).rawgId(350L).name("Hades").status(GameStatus.PLAYING)
                .build());
        entityManager.persistAndFlush(Game.builder()
                .userId(2L).rawgId(350L).name("Hades").status(GameStatus.WISHLIST)
                .build());
        entityManager.persistAndFlush(Game.builder()
                .userId(1L).rawgId(360L).name("Celeste").status(GameStatus.COMPLETED)
                .build());

        List<Game> result = gameRepository.findByRawgId(350L);

        assertEquals(2, result.size());
    }

    @Test
    void shouldNotAllowNullName() {
        Game game = Game.builder()
                .userId(1L)
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
                .userId(1L)
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
