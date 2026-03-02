package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.user.UpdateProfileRequest;
import com.portfolio.silver_lady_s.dto.user.UserProfileResponse;
import com.portfolio.silver_lady_s.security.CurrentUser;
import com.portfolio.silver_lady_s.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponse me() {
        Long userId = CurrentUser.principal().getUserId();
        return userService.me(userId);
    }

    @PutMapping("/me")
    public UserProfileResponse update(@Valid @RequestBody UpdateProfileRequest req) {
        Long userId = CurrentUser.principal().getUserId();
        return userService.updateMe(userId, req);
    }
}
