package com.portfolio.silver_lady_s.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.portfolio.silver_lady_s.service.AuthService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final AuthService authService;

    @Value("${app.telegram.webhook-secret:}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public void handleUpdate(
            @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false) String secret,
            @RequestBody TelegramUpdate update) {

        if (!webhookSecret.isBlank() && !webhookSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (update.getMessage() == null || update.getMessage().getText() == null) return;

        String text = update.getMessage().getText().trim();
        Long chatId = update.getMessage().getChat().getId();

        if (text.startsWith("/start ")) {
            String linkToken = text.substring(7).trim();
            authService.linkTelegramAndSendOtp(chatId, linkToken);
        }
    }

    // ── Telegram update DTOs ──────────────────────────────────────────────────

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramUpdate {
        private TelegramMessage message;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramMessage {
        private TelegramChat chat;
        private String text;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TelegramChat {
        private Long id;
    }
}
