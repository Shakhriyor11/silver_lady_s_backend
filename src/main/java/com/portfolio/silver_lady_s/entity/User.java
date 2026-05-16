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

    @Column(length = 6)
    private String otp;

    @Column(name = "otp_expires_at")
    private Instant otpExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role = UserRole.USER;
}
