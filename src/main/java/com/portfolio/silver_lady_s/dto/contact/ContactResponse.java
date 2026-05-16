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
    private Long productId;
    private String productName;
    private String subject;
    private String message;
    private boolean read;
    private boolean adminInitiated;
    private String adminReply;
    private Instant repliedAt;
    private Instant createdAt;
}
