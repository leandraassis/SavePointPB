package com.assis.gamelog.repository;

import com.assis.gamelog.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByUserId(Long userId);
    Optional<Game> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndRawgId(Long userId, Long rawgId);
}
