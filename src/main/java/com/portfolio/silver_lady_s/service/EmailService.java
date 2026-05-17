package com.portfolio.silver_lady_s.service;

public interface EmailService {
    void sendReplyNotification(String toEmail, String toName, String subject, String adminReply);
    void sendAdminMessage(String toEmail, String toName, String subject, String message);
    void sendOtpEmail(String toEmail, String toName, String otp, long ttlMinutes);
}
