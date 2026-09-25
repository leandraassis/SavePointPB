package com.assis.gamelog.controller;

import com.assis.gamelog.dto.request.LoginDTO;
import com.assis.gamelog.dto.request.RegisterDTO;
import com.assis.gamelog.dto.response.AuthResponseDTO;
import com.assis.gamelog.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final AuthService authService;

    @Value("${jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Value("${jwt.refresh-cookie-secure}")
    private boolean refreshCookieSecure;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid RegisterDTO dto) {
        return withRefreshCookie(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginDTO dto) {
        return withRefreshCookie(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        return withRefreshCookie(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        authService.logout(refreshToken);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie("", Duration.ZERO).toString())
                .build();
    }

    private ResponseEntity<AuthResponseDTO> withRefreshCookie(AuthResponseDTO response) {
        ResponseCookie cookie = buildRefreshCookie(response.getRefreshToken(), refreshTokenExpiration);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
    }

    private ResponseCookie buildRefreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(maxAge)
                .build();
    }
}
