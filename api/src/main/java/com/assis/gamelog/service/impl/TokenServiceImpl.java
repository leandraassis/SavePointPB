package com.assis.gamelog.service.impl;

import com.assis.gamelog.exception.InvalidRefreshTokenException;
import com.assis.gamelog.model.RefreshToken;
import com.assis.gamelog.model.User;
import com.assis.gamelog.repository.RefreshTokenRepository;
import com.assis.gamelog.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.access-token-expiration}")
    private Duration accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gamelog")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenExpiration))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public String createRefreshToken(Long userId) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(hash(token))
                .expiresAt(LocalDateTime.now().plus(refreshTokenExpiration))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Override
    public Long consumeRefreshToken(String token) {
        if(token == null) throw new InvalidRefreshTokenException("Invalid refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if(refreshToken.isRevoked()) {
            revokeAllFromUser(refreshToken.getUserId());
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        if(refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) throw new InvalidRefreshTokenException("Refresh token expired");

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getUserId();
    }

    @Override
    public void revokeRefreshToken(String token) {
        if(token == null) return;

        refreshTokenRepository.findByTokenHash(hash(token)).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    private void revokeAllFromUser(Long userId) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
        activeTokens.forEach(refreshToken -> refreshToken.setRevoked(true));
        refreshTokenRepository.saveAll(activeTokens);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
