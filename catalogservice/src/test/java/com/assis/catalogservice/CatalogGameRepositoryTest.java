package com.assis.catalogservice;

import com.assis.catalogservice.model.CatalogGame;
import com.assis.catalogservice.repository.CatalogGameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CatalogGameRepositoryTest {

    @Autowired
    private CatalogGameRepository catalogGameRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistGameAndGenerateCachedAt() {
        CatalogGame saved = catalogGameRepository.save(catalogGame(100L));

        assertNotNull(saved.getId());
        assertNotNull(saved.getCachedAt());
    }

    @Test
    void shouldFindGameByRawgId() {
        entityManager.persistAndFlush(catalogGame(100L));

        assertTrue(catalogGameRepository.findByRawgId(100L).isPresent());
        assertTrue(catalogGameRepository.existsByRawgId(100L));
        assertFalse(catalogGameRepository.findByRawgId(200L).isPresent());
        assertFalse(catalogGameRepository.existsByRawgId(200L));
    }

    @Test
    void shouldNotAllowDuplicateRawgId() {
        catalogGameRepository.saveAndFlush(catalogGame(100L));

        assertThrows(DataIntegrityViolationException.class, () -> catalogGameRepository.saveAndFlush(catalogGame(100L)));
    }

    @Test
    void shouldRequireName() {
        CatalogGame game = catalogGame(100L);
        game.setName(null);

        assertThrows(DataIntegrityViolationException.class, () -> catalogGameRepository.saveAndFlush(game));
    }

    private CatalogGame catalogGame(Long rawgId) {
        return CatalogGame.builder()
                .rawgId(rawgId)
                .name("Elden Ring")
                .imageUrl("https://img/elden.jpg")
                .build();
    }
}
