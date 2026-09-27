package com.assis.catalogservice.dto.event;

import lombok.Data;

@Data
public class CatalogGameResolvedEvent {
    private String eventId;
    private Long rawgId;
    private String name;
    private String imageUrl;
}
