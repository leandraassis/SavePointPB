package com.assis.gamelog;

import com.assis.gamelog.dto.request.LoginDTO;
import com.assis.gamelog.dto.request.RegisterDTO;
import com.assis.gamelog.dto.response.AuthResponseDTO;
import com.assis.gamelog.exception.InvalidCredentialsException;
import com.assis.gamelog.exception.InvalidRefreshTokenException;
import com.assis.gamelog.exception.UserAlreadyExistsException;
import com.assis.gamelog.model.User;
import com.assis.gamelog.repository.UserRepository;
import com.assis.gamelog.service.TokenService;
import com.assis.gamelog.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldRegisterUserWithNormalizedEmailAndEncodedPassword() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(" player ");
        dto.setEmail(" Player@Email.com ");
        dto.setPassword("senha1234");
        when(userRepository.existsByEmail("player@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(tokenService.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenService.createRefreshToken(1L)).thenReturn("refresh");

        AuthResponseDTO response = authService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("player@email.com", captor.getValue().getEmail());
        assertEquals("player", captor.getValue().getUsername());
        assertEquals("hash", captor.getValue().getPassword());
        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals(1L, response.getUser().getId());
    }

    @Test
    void shouldThrowWhenEmailAlreadyRegistered() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("player");
        dto.setEmail("player@email.com");
        dto.setPassword("senha1234");
        when(userRepository.existsByEmail("player@email.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginWithValidCredentials() {
        User user = user();
        when(userRepository.findByEmail("player@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha1234", "hash")).thenReturn(true);
        when(tokenService.generateAccessToken(user)).thenReturn("access");

        AuthResponseDTO response = authService.login(loginDTO("senha1234"));

        assertEquals("access", response.getAccessToken());
    }

    @Test
    void shouldThrowWhenPasswordIsWrong() {
        when(userRepository.findByEmail("player@email.com")).thenReturn(Optional.of(user()));
        when(passwordEncoder.matches("errada123", "hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginDTO("errada123")));
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmail("player@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginDTO("senha1234")));
    }

    @Test
    void shouldCreateNewSessionOnRefresh() {
        User user = user();
        when(tokenService.consumeRefreshToken("old")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tokenService.createRefreshToken(1L)).thenReturn("new");

        AuthResponseDTO response = authService.refresh("old");

        assertEquals("new", response.getRefreshToken());
    }

    @Test
    void shouldThrowOnRefreshWhenUserNoLongerExists() {
        when(tokenService.consumeRefreshToken("old")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh("old"));
    }

    @Test
    void shouldRevokeRefreshTokenOnLogout() {
        authService.logout("token");

        verify(tokenService).revokeRefreshToken("token");
    }

    private LoginDTO loginDTO(String password) {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("Player@Email.com");
        dto.setPassword(password);
        return dto;
    }

    private User user() {
        return User.builder()
                .id(1L)
                .email("player@email.com")
                .username("player")
                .password("hash")
                .build();
    }
}
