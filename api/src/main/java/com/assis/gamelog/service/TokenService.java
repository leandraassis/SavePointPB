package com.assis.gamelog.service;

import com.assis.gamelog.model.User;

public interface TokenService {
    String generateAccessToken(User user);
    String createRefreshToken(Long userId);
    Long consumeRefreshToken(String refreshToken);
    void revokeRefreshToken(String refreshToken);
}
