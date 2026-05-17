package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.service.TelegramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class TelegramServiceImpl implements TelegramService {

    private final RestClient restClient = RestClient.create();

    @Value("${app.telegram.bot-token:}")
    private String botToken;

    @Value("${app.telegram.bot-username:}")
    private String botUsername;

    @Value("${app.telegram.enabled:false}")
    private boolean enabled;

    @Override
    public boolean isEnabled() {
        return enabled && !botToken.isBlank();
    }

    @Override
    public String getBotUrl(String linkToken) {
        return "https://t.me/" + botUsername + "?start=" + linkToken;
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        if (!isEnabled()) {
            log.debug("Telegram disabled — skipping message to {}", chatId);
            return;
        }
        try {
            restClient.post()
                    .uri("https://api.telegram.org/bot{token}/sendMessage", botToken)
                    .body(Map.of("chat_id", chatId, "text", text))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Telegram message sent to chat {}", chatId);
        } catch (Exception e) {
            log.warn("Telegram sendMessage failed for chat {}: {}", chatId, e.getMessage());
        }
    }
}
