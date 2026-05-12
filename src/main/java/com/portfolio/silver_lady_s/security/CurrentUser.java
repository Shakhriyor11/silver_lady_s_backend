package com.portfolio.silver_lady_s.security;

import com.portfolio.silver_lady_s.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {}

    public static UserPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            throw new UnauthorizedException("Unauthorized");
        }

        return up;
    }
}
