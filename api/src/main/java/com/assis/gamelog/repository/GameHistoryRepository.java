package com.assis.gamelog.repository;

import com.assis.gamelog.model.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {
    List<GameHistory> findByUserIdAndGameIdOrderByChangedAtDesc(Long userId, Long gameId);
}
