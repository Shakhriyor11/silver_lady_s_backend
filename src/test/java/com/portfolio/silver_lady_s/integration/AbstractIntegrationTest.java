package com.portfolio.silver_lady_s.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.silver_lady_s.dto.auth.AuthResponse;
import com.portfolio.silver_lady_s.dto.auth.LoginRequest;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.entity.UserRole;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration testlar uchun asosiy class.
 *
 * Testcontainers orqali PostgreSQL konteyner avtomatik ishga tushadi —
 * alohida DB talab qilinmaydi. CI/CD va local muhitlarda bir xil ishlaydi.
 *
 * Har bir test oldidan barcha jadvallar tozalanadi (TRUNCATE ... CASCADE).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired private DataSource dataSource;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;

    @BeforeEach
    void resetDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    TRUNCATE TABLE
                        order_items, orders,
                        cart_items, carts,
                        product_views, product_images, products,
                        categories,
                        refresh_tokens,
                        contact_messages,
                        users
                    RESTART IDENTITY CASCADE
                    """);
        }
    }

    protected User createUser(String email, String password, UserRole role) {
        User u = new User();
        u.setFullName("Test User");
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setRole(role);
        return userRepository.save(u);
    }

    protected String loginToken(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        String body = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(body, AuthResponse.class).getAccessToken();
    }

    /** Admin yaratib login orqali to'liq AuthResponse (access + refresh token) qaytaradi. */
    protected AuthResponse adminLoginResponse(String email, String password) throws Exception {
        createUser(email, password, UserRole.ADMIN);

        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        String body = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(body, AuthResponse.class);
    }

    /** USER role uchun JWT to'g'ridan-to'g'ri generatsiya qiladi (login endpoint'idan o'tmaydi). */
    protected String userToken() {
        User user = createUser("user@test.com", "User1234!", UserRole.USER);
        return jwtService.generateAccessToken(user);
    }

    /** Berilgan email bilan yangi USER yaratib, uning JWT'sini qaytaradi. */
    protected String tokenForNewUser(String email, String password) {
        User user = createUser(email, password, UserRole.USER);
        return jwtService.generateAccessToken(user);
    }

    protected String adminToken() throws Exception {
        createUser("admin@test.com", "Admin1234!", UserRole.ADMIN);
        return loginToken("admin@test.com", "Admin1234!");
    }

    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
