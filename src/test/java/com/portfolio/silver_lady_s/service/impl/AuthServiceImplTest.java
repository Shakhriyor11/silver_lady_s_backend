package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.auth.AuthResponse;
import com.portfolio.silver_lady_s.dto.auth.LoginRequest;
import com.portfolio.silver_lady_s.dto.auth.RegisterRequest;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.entity.UserRole;
import com.portfolio.silver_lady_s.exception.ConflictException;
import com.portfolio.silver_lady_s.exception.UnauthorizedException;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthServiceImpl authService;

    // ── register ─────────────────────────────────────────────────────────────────

    @Test
    void register_newEmail_returnsToken() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Ali Valiyev");
        req.setEmail("ali@example.com");
        req.setPassword("secret123");

        when(userRepository.existsByEmailIgnoreCase("ali@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse result = authService.register(req);

        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailNormalized_lowercaseAndTrimmed() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Ali");
        req.setEmail("  ALI@EXAMPLE.COM  ");
        req.setPassword("pass123");

        when(userRepository.existsByEmailIgnoreCase("ali@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any())).thenReturn("token");

        authService.register(req);

        verify(userRepository).existsByEmailIgnoreCase("ali@example.com");
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Ali");
        req.setEmail("ali@example.com");
        req.setPassword("pass");

        when(userRepository.existsByEmailIgnoreCase("ali@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void register_setsRoleUser() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Ali");
        req.setEmail("ali@example.com");
        req.setPassword("pass");

        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any())).thenReturn("token");

        authService.register(req);

        verify(userRepository).save(argThat(u -> u.getRole() == UserRole.USER));
    }

    // ── login ────────────────────────────────────────────────────────────────────

    @Test
    void login_correctCredentials_returnsToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("ali@example.com");
        req.setPassword("secret123");

        User user = makeUser(1L, "ali@example.com", "hashed");

        when(userRepository.findByEmailIgnoreCase("ali@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("jwt-token");

        AuthResponse result = authService.login(req);

        assertThat(result.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        LoginRequest req = new LoginRequest();
        req.setEmail("ali@example.com");
        req.setPassword("wrong");

        User user = makeUser(1L, "ali@example.com", "hashed");

        when(userRepository.findByEmailIgnoreCase("ali@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_unknownEmail_throwsUnauthorized() {
        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@example.com");
        req.setPassword("pass");

        when(userRepository.findByEmailIgnoreCase("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_emailNormalized() {
        LoginRequest req = new LoginRequest();
        req.setEmail("  ALI@EXAMPLE.COM  ");
        req.setPassword("pass");

        when(userRepository.findByEmailIgnoreCase("ali@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(UnauthorizedException.class);

        verify(userRepository).findByEmailIgnoreCase("ali@example.com");
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private User makeUser(Long id, String email, String passwordHash) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setFullName("Test User");
        u.setRole(UserRole.USER);
        return u;
    }
}
