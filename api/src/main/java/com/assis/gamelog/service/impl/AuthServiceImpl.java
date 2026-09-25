package com.assis.gamelog.service.impl;

import com.assis.gamelog.dto.request.LoginDTO;
import com.assis.gamelog.dto.request.RegisterDTO;
import com.assis.gamelog.dto.response.AuthResponseDTO;
import com.assis.gamelog.dto.response.UserResponseDTO;
import com.assis.gamelog.exception.InvalidCredentialsException;
import com.assis.gamelog.exception.InvalidRefreshTokenException;
import com.assis.gamelog.exception.UserAlreadyExistsException;
import com.assis.gamelog.model.User;
import com.assis.gamelog.repository.UserRepository;
import com.assis.gamelog.service.AuthService;
import com.assis.gamelog.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    public AuthResponseDTO register(RegisterDTO dto) {
        String email = normalizeEmail(dto.getEmail());
        if(userRepository.existsByEmail(email)) throw new UserAlreadyExistsException("User already exists");

        User user = User.builder().email(email)
                .username(dto.getUsername().trim())
                .password(passwordEncoder.encode(dto.getPassword())).build();

        User savedUser = userRepository.save(user);
        return createSession(savedUser);
    }

    @Override
    public AuthResponseDTO login(LoginDTO dto) {
        User user = userRepository.findByEmail(normalizeEmail(dto.getEmail()))
                .filter(found -> passwordEncoder.matches(dto.getPassword(), found.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        return createSession(user);
    }

    @Override
    public AuthResponseDTO refresh(String refreshToken) {
        Long userId = tokenService.consumeRefreshToken(refreshToken);
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        return createSession(user);
    }

    @Override
    public void logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
    }

    private AuthResponseDTO createSession(User user) {
        AuthResponseDTO dto = new AuthResponseDTO();
        dto.setAccessToken(tokenService.generateAccessToken(user));
        dto.setRefreshToken(tokenService.createRefreshToken(user.getId()));
        dto.setUser(toUserDTO(user));
        return dto;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private UserResponseDTO toUserDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
