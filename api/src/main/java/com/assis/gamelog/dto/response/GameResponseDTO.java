package com.assis.gamelog.dto.response;

import com.assis.gamelog.model.GameStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameResponseDTO {

    private Long id;
    private Long rawgId;
    private String name;
    private String imageUrl;
    private GameStatus status;
    private Integer rating;
    private LocalDateTime createdAt;


}
