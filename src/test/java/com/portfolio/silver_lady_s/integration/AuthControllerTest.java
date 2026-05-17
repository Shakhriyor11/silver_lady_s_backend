package com.portfolio.silver_lady_s.integration;

import com.portfolio.silver_lady_s.dto.auth.AuthResponse;
import com.portfolio.silver_lady_s.dto.auth.LoginRequest;
import com.portfolio.silver_lady_s.dto.auth.RefreshRequest;
import com.portfolio.silver_lady_s.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends AbstractIntegrationTest {

    // ── register ─────────────────────────────────────────────────────────────────

    @Test
    void register_newUser_returns201WithBothTokens() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("ali@example.com");
        req.setPassword("Secret123");
        req.setFullName("Ali Valiyev");

        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn().getResponse().getContentAsString();

        AuthResponse resp = objectMapper.readValue(body, AuthResponse.class);
        assertThat(resp.getAccessToken()).isNotBlank();
        assertThat(resp.getRefreshToken()).isNotBlank();
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        register("ali@example.com", "Secret123");

        RegisterRequest req = new RegisterRequest();
        req.setEmail("ali@example.com");
        req.setPassword("AnotherPass1");
        req.setFullName("Ali 2");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_emailCaseInsensitiveDuplicate_returns409() throws Exception {
        register("ali@example.com", "Secret123");

        RegisterRequest req = new RegisterRequest();
        req.setEmail("ALI@EXAMPLE.COM");
        req.setPassword("AnotherPass1");
        req.setFullName("Ali Upper");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("12345"); // min 6 char
        req.setFullName("Test");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("not-an-email");
        req.setPassword("Secret123");
        req.setFullName("Test");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingFullName_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setPassword("Secret123");
        // fullName set qilinmagan

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest());
    }

    // ── login ────────────────────────────────────────────────────────────────────

    @Test
    void login_correctCredentials_returns200WithBothTokens() throws Exception {
        register("ali@example.com", "Secret123");

        LoginRequest req = new LoginRequest();
        req.setEmail("ali@example.com");
        req.setPassword("Secret123");

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse resp = objectMapper.readValue(body, AuthResponse.class);
        assertThat(resp.getAccessToken()).isNotBlank();
        assertThat(resp.getRefreshToken()).isNotBlank();
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        register("ali@example.com", "Secret123");

        LoginRequest req = new LoginRequest();
        req.setEmail("ali@example.com");
        req.setPassword("WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownEmail_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@example.com");
        req.setPassword("AnyPass123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── refresh ──────────────────────────────────────────────────────────────────

    @Test
    void refresh_validToken_returnsNewRotatedTokens() throws Exception {
        AuthResponse first = register("ali@example.com", "Secret123");

        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken(first.getRefreshToken());

        String body = mockMvc.perform(post("/api/auth/refresh")
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

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_revokedToken_returns401() throws Exception {
        AuthResponse first = register("ali@example.com", "Secret123");
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken(first.getRefreshToken());

        // Birinchi refresh — token rotatsiya qiladi
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk());

        // Bir xil (endi bekor qilingan) token bilan qayta refresh
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── logout ───────────────────────────────────────────────────────────────────

    @Test
    void logout_validToken_returns204() throws Exception {
        AuthResponse auth = register("ali@example.com", "Secret123");

        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken(auth.getRefreshToken());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_unknownToken_returns204() throws Exception {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("unknown-token-uuid-value");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_thenRefresh_returns401() throws Exception {
        AuthResponse auth = register("ali@example.com", "Secret123");
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken(auth.getRefreshToken());

        // Logout qilamiz
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isNoContent());

        // O'sha token bilan refresh qilishga urinish
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── logout-all ────────────────────────────────────────────────────────────────

    @Test
    void logoutAll_authenticated_returns204() throws Exception {
        String token = userToken();

        mockMvc.perform(post("/api/auth/logout-all")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/logout-all"))
                .andExpect(status().isUnauthorized());
    }
}
