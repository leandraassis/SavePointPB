package com.assis.gamelog.repository;

import com.assis.gamelog.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
    boolean existsByRawgId(Long rawgId);
}
