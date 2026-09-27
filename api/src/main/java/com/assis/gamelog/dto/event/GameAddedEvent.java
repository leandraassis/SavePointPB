package com.assis.gamelog.dto.event;

import lombok.Data;

@Data
public class GameAddedEvent {
    private String eventId;
    private Long rawgId;
}
