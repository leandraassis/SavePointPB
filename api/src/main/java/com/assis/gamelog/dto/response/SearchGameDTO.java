package com.assis.gamelog.dto.response;

import lombok.Data;

@Data
public class SearchGameDTO {
    private Long rawgId;
    private String name;
    private String imageUrl;
}
