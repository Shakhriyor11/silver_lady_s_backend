package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_phone", columnList = "phone")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 40)
    private String phone;

    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private boolean phoneVerified = false;

    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private boolean emailVerified = false;

    @Column(length = 6)
    private String otp;

    @Column(name = "otp_expires_at")
    private Instant otpExpiresAt;

    @Column(name = "email_otp", length = 6)
    private String emailOtp;

    @Column(name = "email_otp_expires_at")
    private Instant emailOtpExpiresAt;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "telegram_link_token", length = 36)
    private String telegramLinkToken;

    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private boolean telegramVerified = false;

    @Column(name = "telegram_otp", length = 6)
    private String telegramOtp;

    @Column(name = "telegram_otp_expires_at")
    private Instant telegramOtpExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role = UserRole.USER;
}
