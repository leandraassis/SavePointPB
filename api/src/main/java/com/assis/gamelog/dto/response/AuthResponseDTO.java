package com.assis.gamelog.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class AuthResponseDTO {
    private String accessToken;
    private UserResponseDTO user;

    @JsonIgnore
    private String refreshToken;
}
