package com.portfolio.silver_lady_s.service;

public interface TelegramService {
    void sendMessage(Long chatId, String text);
    boolean isEnabled();
    String getBotUrl(String linkToken);
}
