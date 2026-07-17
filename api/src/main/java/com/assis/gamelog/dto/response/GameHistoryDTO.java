package com.assis.gamelog.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameHistoryDTO {
    private String fieldName;
    private String oldValue;
    private String newValue;
    private LocalDateTime changedAt;
}