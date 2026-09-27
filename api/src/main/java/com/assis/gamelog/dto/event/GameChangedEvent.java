package com.assis.gamelog.dto.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameChangedEvent {
    private String eventId;
    private Long userId;
    private Long gameId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private LocalDateTime occurredAt;
}
