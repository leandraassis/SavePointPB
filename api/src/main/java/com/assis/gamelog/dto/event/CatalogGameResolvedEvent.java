package com.assis.gamelog.dto.event;

import lombok.Data;

@Data
public class CatalogGameResolvedEvent {
    private String eventId;
    private Long rawgId;
    private String name;
    private String imageUrl;
}
