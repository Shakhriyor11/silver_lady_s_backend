package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.user.ChangePasswordRequest;
import com.portfolio.silver_lady_s.dto.user.UpdateProfileRequest;
import com.portfolio.silver_lady_s.dto.user.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserProfileResponse me(Long userId);
    UserProfileResponse updateMe(Long userId, UpdateProfileRequest req);
    void changePassword(Long userId, ChangePasswordRequest req);
    Page<UserProfileResponse> listUsers(String q, Pageable pageable);
}
