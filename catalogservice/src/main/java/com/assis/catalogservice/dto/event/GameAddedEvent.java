package com.assis.catalogservice.dto.event;

import lombok.Data;

@Data
public class GameAddedEvent {
    private String eventId;
    private Long rawgId;
}
