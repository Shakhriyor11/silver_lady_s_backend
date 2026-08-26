package com.portfolio.silver_lady_s.integration;

import com.portfolio.silver_lady_s.dto.auth.AuthResponse;
import com.portfolio.silver_lady_s.dto.auth.LoginRequest;
import com.portfolio.silver_lady_s.dto.auth.RefreshRequest;
import com.portfolio.silver_lady_s.entity.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends AbstractIntegrationTest {

    // ── login ────────────────────────────────────────────────────────────────────

    @Test
    void login_adminCredentials_returns200WithBothTokens() throws Exception {
        createUser("admin@example.com", "Secret123", UserRole.ADMIN);

        LoginRequest req = new LoginRequest();
        req.setEmail("admin@example.com");
        req.setPassword("Secret123");

        String body = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn().getResponse().getContentAsString();

        AuthResponse resp = objectMapper.readValue(body, AuthResponse.class);
        assertThat(resp.getAccessToken()).isNotBlank();
        assertThat(resp.getRefreshToken()).isNotBlank();
    }

    @Test
    void login_asCashier_succeeds() throws Exception {
        createUser("cashier@example.com", "Secret123", UserRole.CASHIER);

        LoginRequest req = new LoginRequest();
        req.setEmail("cashier@example.com");
        req.setPassword("Secret123");

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString());
    }

    @Test
    void login_regularUser_returns401() throws Exception {
        createUser("user@example.com", "Secret123", UserRole.USER);

        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("Secret123");

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        createUser("admin@example.com", "Secret123", UserRole.ADMIN);

        LoginRequest req = new LoginRequest();
        req.setEmail("admin@example.com");
        req.setPassword("WrongPassword");

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownEmail_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@example.com");
        req.setPassword("AnyPass123");

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── refresh ──────────────────────────────────────────────────────────────────

    @Test
    void refresh_validToken_returnsNewRotatedTokens() throws Exception {
        AuthResponse first = adminLoginResponse("admin@example.com", "Secret123");

        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken(first.getRefreshToken());

        String body = mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse refreshed = objectMapper.readValue(body, AuthResponse.class);
        assertThat(refreshed.getAccessToken()).isNotBlank();
        assertThat(refreshed.getRefreshToken()).isNotBlank();
        assertThat(refreshed.getRefreshToken()).isNotEqualTo(first.getRefreshToken());
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("totally-invalid-uuid-token");

        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_revokedToken_returns401() throws Exception {
        AuthResponse first = adminLoginResponse("admin@example.com", "Secret123");
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken(first.getRefreshToken());

        // Birinchi refresh — token rotatsiya qiladi
        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk());

        // Bir xil (endi bekor qilingan) token bilan qayta refresh
        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── logout ───────────────────────────────────────────────────────────────────

    @Test
    void logout_validToken_returns204() throws Exception {
        AuthResponse auth = adminLoginResponse("admin@example.com", "Secret123");

        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken(auth.getRefreshToken());

        mockMvc.perform(post("/api/admin/auth/logout")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_unknownToken_returns204() throws Exception {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("unknown-token-uuid-value");

        mockMvc.perform(post("/api/admin/auth/logout")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_thenRefresh_returns401() throws Exception {
        AuthResponse auth = adminLoginResponse("admin@example.com", "Secret123");
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken(auth.getRefreshToken());

        // Logout qilamiz
        mockMvc.perform(post("/api/admin/auth/logout")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isNoContent());

        // O'sha token bilan refresh qilishga urinish
        mockMvc.perform(post("/api/admin/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── logout-all ────────────────────────────────────────────────────────────────

    @Test
    void logoutAll_authenticated_returns204() throws Exception {
        String token = userToken();

        mockMvc.perform(post("/api/admin/auth/logout-all")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/auth/logout-all"))
                .andExpect(status().isUnauthorized());
    }
}
