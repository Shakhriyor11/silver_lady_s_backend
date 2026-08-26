package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.user.ChangePasswordRequest;
import com.portfolio.silver_lady_s.dto.user.CreateCashierRequest;
import com.portfolio.silver_lady_s.dto.user.UpdateProfileRequest;
import com.portfolio.silver_lady_s.dto.user.UserProfileResponse;
import com.portfolio.silver_lady_s.security.CurrentUser;
import com.portfolio.silver_lady_s.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
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

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        Long userId = CurrentUser.principal().getUserId();
        userService.changePassword(userId, req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserProfileResponse> listUsers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0")  @Min(0)          int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return userService.listUsers(q, PageRequest.of(page, size));
    }

    @PostMapping("/cashiers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> createCashier(@Valid @RequestBody CreateCashierRequest req) {
        UserProfileResponse created = userService.createCashier(req);
        return ResponseEntity.created(URI.create("/api/users/" + created.getId())).body(created);
    }
}
