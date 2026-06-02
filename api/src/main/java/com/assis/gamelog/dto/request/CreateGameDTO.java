package com.assis.gamelog.dto.request;

import com.assis.gamelog.model.GameStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGameDTO {

    @NotNull
    private Long rawgId;

    @NotNull
    private GameStatus gameStatus;

    @Min(1)
    @Max(2)
    private Integer rating;
}
