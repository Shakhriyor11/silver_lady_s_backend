package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String from;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.email.enabled:false}")
    private boolean enabled;

    @Override
    public void sendReplyNotification(String toEmail, String toName, String subject, String adminReply) {
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
                  <div style="background:#c9a84c;padding:20px 24px;border-radius:8px 8px 0 0">
                    <h2 style="color:#fff;margin:0;font-size:18px">Silver Lady's — Javob keldi</h2>
                  </div>
                  <div style="background:#fafafa;padding:24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 8px 8px">
                    <p style="color:#374151;margin-top:0">Assalomu alaykum, <strong>%s</strong>!</p>
                    <p style="color:#374151">Sizning <strong>"%s"</strong> mavzusidagi xabaringizga javob berildi:</p>
                    <div style="background:#fff;border-left:4px solid #c9a84c;padding:16px;border-radius:0 6px 6px 0;margin:16px 0">
                      <p style="color:#1f2937;margin:0;white-space:pre-wrap">%s</p>
                    </div>
                    <p style="color:#6b7280;font-size:13px">Saytimizga kirib barcha xabarlaringizni ko'rishingiz mumkin.</p>
                  </div>
                </div>
                """.formatted(toName, subject, adminReply);

        send(toEmail, "Silver Lady's: " + subject + " — javob berildi", html);
    }

    @Override
    public void sendAdminMessage(String toEmail, String toName, String subject, String message) {
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
                  <div style="background:#c9a84c;padding:20px 24px;border-radius:8px 8px 0 0">
                    <h2 style="color:#fff;margin:0;font-size:18px">Silver Lady's — Yangi xabar</h2>
                  </div>
                  <div style="background:#fafafa;padding:24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 8px 8px">
                    <p style="color:#374151;margin-top:0">Assalomu alaykum, <strong>%s</strong>!</p>
                    <p style="color:#374151">Sizga <strong>Silver Lady's</strong> do'konidan yangi xabar keldi:</p>
                    <p style="color:#374151;font-weight:600">%s</p>
                    <div style="background:#fff;border-left:4px solid #c9a84c;padding:16px;border-radius:0 6px 6px 0;margin:16px 0">
                      <p style="color:#1f2937;margin:0;white-space:pre-wrap">%s</p>
                    </div>
                    <p style="color:#6b7280;font-size:13px">Saytimizga kirib javob berishingiz mumkin.</p>
                  </div>
                </div>
                """.formatted(toName, subject, message);

        send(toEmail, "Silver Lady's: " + subject, html);
    }

    @Override
    public void sendOtpEmail(String toEmail, String toName, String otp, long ttlMinutes) {
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
                  <div style="background:#c9a84c;padding:20px 24px;border-radius:8px 8px 0 0">
                    <h2 style="color:#fff;margin:0;font-size:18px">Silver Lady's — Tasdiqlash kodi</h2>
                  </div>
                  <div style="background:#fafafa;padding:24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 8px 8px">
                    <p style="color:#374151;margin-top:0">Assalomu alaykum, <strong>%s</strong>!</p>
                    <p style="color:#374151">Elektron pochtangizni tasdiqlash uchun quyidagi kodni kiriting:</p>
                    <div style="text-align:center;margin:24px 0">
                      <span style="font-size:36px;font-weight:700;letter-spacing:8px;color:#c9a84c;background:#fff;border:2px solid #c9a84c;padding:12px 24px;border-radius:8px">%s</span>
                    </div>
                    <p style="color:#6b7280;font-size:13px">Bu kod <strong>%d daqiqa</strong> ichida amal qiladi.</p>
                    <p style="color:#6b7280;font-size:13px">Agar siz ro'yxatdan o'tmagan bo'lsangiz, ushbu xabarni e'tiborsiz qoldiring.</p>
                  </div>
                </div>
                """.formatted(toName, otp, ttlMinutes);

        send(toEmail, "Silver Lady's — Tasdiqlash kodi: " + otp, html);
    }

    private void send(String to, String subject, String html) {
        if (!enabled) {
            log.debug("Email disabled — skipping send to {}", to);
            return;
        }
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setFrom(from, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mime);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.warn("Email send failed to {}: {}", to, e.getMessage());
        }
    }
}
