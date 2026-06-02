package com.assis.gamelog.dto.rawg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class RawgSearchResponseDTO {

    private List<RawgGameDTO> results;
}
