package com.assis.gamelog;

import com.assis.gamelog.config.SecurityConfig;
import com.assis.gamelog.controller.AuthController;
import com.assis.gamelog.dto.request.LoginDTO;
import com.assis.gamelog.dto.request.RegisterDTO;
import com.assis.gamelog.dto.response.AuthResponseDTO;
import com.assis.gamelog.exception.InvalidCredentialsException;
import com.assis.gamelog.exception.InvalidRefreshTokenException;
import com.assis.gamelog.exception.UserAlreadyExistsException;
import com.assis.gamelog.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "jwt.secret=test-secret-with-at-least-32-bytes!!",
        "jwt.refresh-token-expiration=7d",
        "jwt.refresh-cookie-secure=false"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void shouldRegisterAndSetRefreshTokenCookie() throws Exception {
        when(authService.register(any(RegisterDTO.class))).thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "player", "email": "player@email.com", "password": "senha1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(header().string("Set-Cookie", containsString("refresh_token=refresh")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Strict")))
                .andExpect(header().string("Set-Cookie", containsString("Path=/api/auth")));
    }

    @Test
    void shouldReturn400WhenRegisterIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "player", "email": "email-invalido", "password": "curta"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void shouldReturn409WhenUserAlreadyExists() throws Exception {
        when(authService.register(any(RegisterDTO.class))).thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "player", "email": "player@email.com", "password": "senha1234"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        when(authService.login(any(LoginDTO.class))).thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "player@email.com", "password": "errada123"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void shouldRefreshUsingCookie() throws Exception {
        when(authService.refresh("old")).thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/refresh").cookie(new Cookie("refresh_token", "old")))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("refresh_token=refresh")));
    }

    @Test
    void shouldReturn401WhenRefreshTokenIsInvalid() throws Exception {
        when(authService.refresh(null)).thenThrow(new InvalidRefreshTokenException("Invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLogoutAndClearCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout").cookie(new Cookie("refresh_token", "old")))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

        verify(authService).logout("old");
    }

    private AuthResponseDTO authResponse() {
        AuthResponseDTO dto = new AuthResponseDTO();
        dto.setAccessToken("access");
        dto.setRefreshToken("refresh");
        return dto;
    }
}
