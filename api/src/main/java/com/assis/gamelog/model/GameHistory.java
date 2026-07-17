package com.assis.gamelog.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gameId;

    @Column(nullable = false)
    private String fieldName;

    private String oldValue;

    private String newValue;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        if (changedAt == null) changedAt = LocalDateTime.now();
    }
}
