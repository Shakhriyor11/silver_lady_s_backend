package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.auth.AuthResponse;
import com.portfolio.silver_lady_s.dto.auth.LoginRequest;
import com.portfolio.silver_lady_s.dto.auth.RegisterRequest;
import com.portfolio.silver_lady_s.entity.RefreshToken;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.entity.UserRole;
import com.portfolio.silver_lady_s.exception.ConflictException;
import com.portfolio.silver_lady_s.exception.UnauthorizedException;
import com.portfolio.silver_lady_s.repository.RefreshTokenRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.security.JwtService;
import com.portfolio.silver_lady_s.service.SmsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private SmsService smsService;

    @InjectMocks private AuthServiceImpl authService;

    // ── register ─────────────────────────────────────────────────────────────────

    @Test
    void register_newEmail_returnsBothTokens() {
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
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse result = authService.register(req);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isNotBlank();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
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
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

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
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.register(req);

        verify(userRepository).save(argThat(u -> u.getRole() == UserRole.USER));
    }

    // ── login ────────────────────────────────────────────────────────────────────

    @Test
    void login_correctCredentials_returnsBothTokens() {
        LoginRequest req = new LoginRequest();
        req.setEmail("ali@example.com");
        req.setPassword("secret123");

        User user = makeUser(1L, "ali@example.com", "hashed");

        when(userRepository.findByEmailIgnoreCase("ali@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse result = authService.login(req);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isNotBlank();
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
                .isInstanceOf(UnauthorizedException.class);
    }

    // ── refresh ──────────────────────────────────────────────────────────────────

    @Test
    void refresh_validToken_rotatesAndReturnsBothTokens() {
        User user = makeUser(1L, "ali@example.com", "hashed");
        RefreshToken rt = makeRefreshToken("old-uuid", user, false,
                Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken("old-uuid")).thenReturn(Optional.of(rt));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");

        AuthResponse result = authService.refresh("old-uuid");

        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isNotBlank();
        assertThat(result.getRefreshToken()).isNotEqualTo("old-uuid");
        assertThat(rt.isRevoked()).isTrue();
    }

    @Test
    void refresh_unknownToken_throwsUnauthorized() {
        when(refreshTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("bad-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void refresh_revokedToken_revokesAllAndThrows() {
        User user = makeUser(1L, "ali@example.com", "hashed");
        RefreshToken rt = makeRefreshToken("stolen-uuid", user, true,
                Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken("stolen-uuid")).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> authService.refresh("stolen-uuid"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("already used");

        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }

    @Test
    void refresh_expiredToken_throwsUnauthorized() {
        User user = makeUser(1L, "ali@example.com", "hashed");
        RefreshToken rt = makeRefreshToken("expired-uuid", user, false,
                Instant.now().minusSeconds(1));

        when(refreshTokenRepository.findByToken("expired-uuid")).thenReturn(Optional.of(rt));
        when(refreshTokenRepository.save(rt)).thenReturn(rt);

        assertThatThrownBy(() -> authService.refresh("expired-uuid"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");

        assertThat(rt.isRevoked()).isTrue();
    }

    // ── logout ───────────────────────────────────────────────────────────────────

    @Test
    void logout_existingToken_revokesIt() {
        User user = makeUser(1L, "ali@example.com", "hashed");
        RefreshToken rt = makeRefreshToken("some-uuid", user, false,
                Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken("some-uuid")).thenReturn(Optional.of(rt));
        when(refreshTokenRepository.save(rt)).thenReturn(rt);

        authService.logout("some-uuid");

        assertThat(rt.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(rt);
    }

    @Test
    void logout_unknownToken_doesNotThrow() {
        when(refreshTokenRepository.findByToken("ghost-uuid")).thenReturn(Optional.empty());

        authService.logout("ghost-uuid");

        verify(refreshTokenRepository, never()).save(any());
    }

    // ── logoutAll ─────────────────────────────────────────────────────────────────

    @Test
    void logoutAll_revokesAllUserTokens() {
        authService.logoutAll(1L);

        verify(refreshTokenRepository).revokeAllByUserId(1L);
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

    private RefreshToken makeRefreshToken(String token, User user,
                                          boolean revoked, Instant expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.setId(1L);
        rt.setToken(token);
        rt.setUser(user);
        rt.setRevoked(revoked);
        rt.setExpiresAt(expiresAt);
        return rt;
    }
}
