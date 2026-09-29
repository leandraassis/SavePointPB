package com.assis.gamelog;

import com.assis.gamelog.exception.InvalidRefreshTokenException;
import com.assis.gamelog.model.RefreshToken;
import com.assis.gamelog.model.User;
import com.assis.gamelog.repository.RefreshTokenRepository;
import com.assis.gamelog.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "accessTokenExpiration", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(tokenService, "refreshTokenExpiration", Duration.ofDays(7));
    }

    @Test
    void shouldGenerateAccessTokenWithUserIdAsSubject() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("1")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(900)).build();
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = tokenService.generateAccessToken(User.builder().id(1L).build());

        assertEquals("token", token);
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        JwtClaimsSet claims = captor.getValue().getClaims();
        assertEquals("1", claims.getSubject());
        assertEquals("gamelog", claims.getClaimAsString("iss"));
        assertEquals(Duration.ofMinutes(15), Duration.between(claims.getIssuedAt(), claims.getExpiresAt()));
    }

    @Test
    void shouldStoreOnlyHashOfRefreshToken() {
        String token = tokenService.createRefreshToken(1L);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();
        assertNotEquals(token, saved.getTokenHash());
        assertEquals(64, saved.getTokenHash().length());
        assertFalse(saved.isRevoked());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now().plusDays(6)));
    }

    @Test
    void shouldConsumeValidRefreshTokenAndRevokeIt() {
        RefreshToken refreshToken = refreshToken(false, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));

        Long userId = tokenService.consumeRefreshToken("token");

        assertEquals(1L, userId);
        assertTrue(refreshToken.isRevoked());
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void shouldRevokeAllUserTokensWhenRevokedTokenIsReused() {
        RefreshToken reused = refreshToken(true, LocalDateTime.now().plusDays(1));
        RefreshToken active = refreshToken(false, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(reused));
        when(refreshTokenRepository.findByUserIdAndRevokedFalse(1L)).thenReturn(List.of(active));

        assertThrows(InvalidRefreshTokenException.class, () -> tokenService.consumeRefreshToken("token"));
        assertTrue(active.isRevoked());
        verify(refreshTokenRepository).saveAll(List.of(active));
    }

    @Test
    void shouldThrowWhenRefreshTokenIsExpired() {
        RefreshToken expired = refreshToken(false, LocalDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

        assertThrows(InvalidRefreshTokenException.class, () -> tokenService.consumeRefreshToken("token"));
        assertFalse(expired.isRevoked());
    }

    @Test
    void shouldThrowWhenRefreshTokenIsUnknownOrNull() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class, () -> tokenService.consumeRefreshToken("token"));
        assertThrows(InvalidRefreshTokenException.class, () -> tokenService.consumeRefreshToken(null));
    }

    @Test
    void shouldRevokeRefreshTokenOnLogout() {
        RefreshToken refreshToken = refreshToken(false, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(refreshToken));

        tokenService.revokeRefreshToken("token");

        assertTrue(refreshToken.isRevoked());
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void shouldIgnoreLogoutWithoutValidToken() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        tokenService.revokeRefreshToken("unknown");
        tokenService.revokeRefreshToken(null);

        verify(refreshTokenRepository, never()).save(any());
    }

    private RefreshToken refreshToken(boolean revoked, LocalDateTime expiresAt) {
        return RefreshToken.builder()
                .userId(1L)
                .tokenHash("hash")
                .revoked(revoked)
                .expiresAt(expiresAt)
                .build();
    }
}
