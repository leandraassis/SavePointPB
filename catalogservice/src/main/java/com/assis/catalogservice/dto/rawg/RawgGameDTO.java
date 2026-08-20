package com.assis.catalogservice.dto.rawg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RawgGameDTO {

    private Long id;
    private String name;
    @JsonProperty("background_image")
    private String backgroundImage;
}

