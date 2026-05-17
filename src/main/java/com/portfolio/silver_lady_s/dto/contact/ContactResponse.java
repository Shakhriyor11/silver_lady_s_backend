package com.portfolio.silver_lady_s.dto.contact;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ContactResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private Long productId;       // null bo'lishi mumkin
    private String productName;   // null bo'lishi mumkin
    private String subject;
    private String message;
    private boolean read;
    private Instant createdAt;
}
