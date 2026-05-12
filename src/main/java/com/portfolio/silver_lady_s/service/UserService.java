package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.user.ChangePasswordRequest;
import com.portfolio.silver_lady_s.dto.user.UpdateProfileRequest;
import com.portfolio.silver_lady_s.dto.user.UserProfileResponse;

public interface UserService {
    UserProfileResponse me(Long userId);
    UserProfileResponse updateMe(Long userId, UpdateProfileRequest req);
    void changePassword(Long userId, ChangePasswordRequest req);
}
