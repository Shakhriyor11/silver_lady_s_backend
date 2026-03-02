package com.portfolio.silver_lady_s.security;

import com.portfolio.silver_lady_s.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String email;
    private UserRole role;
}
