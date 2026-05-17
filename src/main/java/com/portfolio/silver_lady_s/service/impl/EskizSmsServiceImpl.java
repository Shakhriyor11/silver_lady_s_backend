package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EskizSmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(EskizSmsServiceImpl.class);
    private static final String BASE_URL = "https://notify.eskiz.uz/api";

    @Value("${app.sms.eskiz.email:}")
    private String email;

    @Value("${app.sms.eskiz.password:}")
    private String password;

    @Value("${app.sms.eskiz.sender-id:4546}")
    private String senderId;

    @Value("${app.sms.eskiz.enabled:false}")
    private boolean enabled;

    private record TokenState(String token, Instant expiresAt) {
        boolean isValid() {
            return token != null && expiresAt != null && Instant.now().isBefore(expiresAt);
        }
    }

    private final RestClient restClient;
    private final AtomicReference<TokenState> tokenState = new AtomicReference<>(new TokenState(null, null));

    public EskizSmsServiceImpl() {
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    @Override
    public void send(String phone, String message) {
        if (!enabled) {
            log.info("SMS disabled. To={}, message={}", phone, message);
            return;
        }

        String normalized = normalizePhone(phone);
        String token = getToken();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("mobile_phone", normalized);
        body.add("message", message);
        body.add("from", senderId);

        try {
            restClient.post()
                    .uri("/message/sms/send")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("SMS sent to {}", normalized);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", normalized, e.getMessage());
            throw new RuntimeException("SMS sending failed: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private String getToken() {
        TokenState state = tokenState.get();
        if (state.isValid()) return state.token();
        return fetchNewToken();
    }

    @SuppressWarnings("unchecked")
    private synchronized String fetchNewToken() {
        TokenState state = tokenState.get();
        if (state.isValid()) return state.token();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("email", email);
        body.add("password", password);

        Map<String, Object> response = restClient.post()
                .uri("/auth/login")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("data")) {
            throw new RuntimeException("Eskiz.uz auth failed: no token in response");
        }

        Map<String, String> data = (Map<String, String>) response.get("data");
        String newToken = data.get("token");
        tokenState.set(new TokenState(newToken, Instant.now().plus(29, ChronoUnit.DAYS)));
        log.info("Eskiz.uz token refreshed");
        return newToken;
    }

    private String normalizePhone(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("998") && digits.length() == 12) return digits;
        if (digits.length() == 9) return "998" + digits;
        if (digits.startsWith("0") && digits.length() == 10) return "998" + digits.substring(1);
        return digits;
    }
}
