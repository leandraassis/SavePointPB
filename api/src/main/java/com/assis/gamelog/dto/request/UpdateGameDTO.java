package com.assis.gamelog.dto.request;

import com.assis.gamelog.model.GameStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateGameDTO {
    private GameStatus status;

    @Min(1)
    @Max(5)
    private Integer rating;
}
