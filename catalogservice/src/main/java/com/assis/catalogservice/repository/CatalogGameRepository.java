package com.assis.catalogservice.repository;

import com.assis.catalogservice.model.CatalogGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatalogGameRepository extends JpaRepository<CatalogGame, Long> {
    Optional<CatalogGame> findByRawgId(Long rawgId);
    boolean existsByRawgId(Long rawgId);
}
