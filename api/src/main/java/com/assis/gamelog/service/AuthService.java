package com.assis.gamelog.service;

import com.assis.gamelog.dto.request.LoginDTO;
import com.assis.gamelog.dto.request.RegisterDTO;
import com.assis.gamelog.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO register(RegisterDTO dto);
    AuthResponseDTO login(LoginDTO dto);
    AuthResponseDTO refresh(String refreshToken);
    void logout(String refreshToken);
}
