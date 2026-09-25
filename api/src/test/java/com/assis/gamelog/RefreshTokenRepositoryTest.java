package com.assis.gamelog;

import com.assis.gamelog.model.RefreshToken;
import com.assis.gamelog.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindTokenByHash() {
        RefreshToken token = RefreshToken.builder()
                .userId(1L).tokenHash("hash-1").expiresAt(LocalDateTime.now().plusDays(7)).revoked(false)
                .build();
        entityManager.persistAndFlush(token);

        assertTrue(refreshTokenRepository.findByTokenHash("hash-1").isPresent());
        assertTrue(refreshTokenRepository.findByTokenHash("unknown").isEmpty());
    }

    @Test
    void shouldReturnOnlyActiveTokensFromUser() {
        entityManager.persistAndFlush(RefreshToken.builder()
                .userId(1L).tokenHash("active").expiresAt(LocalDateTime.now().plusDays(7)).revoked(false)
                .build());
        entityManager.persistAndFlush(RefreshToken.builder()
                .userId(1L).tokenHash("revoked").expiresAt(LocalDateTime.now().plusDays(7)).revoked(true)
                .build());
        entityManager.persistAndFlush(RefreshToken.builder()
                .userId(2L).tokenHash("other-user").expiresAt(LocalDateTime.now().plusDays(7)).revoked(false)
                .build());

        List<RefreshToken> result = refreshTokenRepository.findByUserIdAndRevokedFalse(1L);

        assertEquals(1, result.size());
        assertEquals("active", result.get(0).getTokenHash());
    }
}
