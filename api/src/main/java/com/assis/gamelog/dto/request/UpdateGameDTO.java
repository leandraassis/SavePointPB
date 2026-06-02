package com.assis.gamelog.dto.request;

import com.assis.gamelog.model.GameStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class UpdateGameDTO {
    private GameStatus gameStatus;

    @Min(1)
    @Max(5)
    private Integer rating;
}
