package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.user.ChangePasswordRequest;
import com.portfolio.silver_lady_s.dto.user.UpdateProfileRequest;
import com.portfolio.silver_lady_s.dto.user.UserProfileResponse;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.entity.UserRole;
import com.portfolio.silver_lady_s.exception.BadRequestException;
import com.portfolio.silver_lady_s.exception.ConflictException;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.RefreshTokenRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.service.MediaStorageService;
import com.portfolio.silver_lady_s.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MediaStorageService mediaStorageService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse me(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toResponse(u);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMe(Long userId, UpdateProfileRequest req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        u.setFullName(req.getFullName().trim());
        u.setPhone(req.getPhone() == null ? null : req.getPhone().trim());

        return toResponse(userRepository.save(u));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), u.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        u.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(u);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileResponse> listUsers(String q, Pageable pageable) {
        return userRepository.searchRegularUsers(q == null ? "" : q.trim(), pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public UserProfileResponse updateAvatar(Long userId, MultipartFile file) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String oldAvatarUrl = u.getAvatarUrl();
        String newAvatarUrl = mediaStorageService.storeInFolder(file, "users/" + userId);

        u.setAvatarUrl(newAvatarUrl);
        User saved = userRepository.save(u);

        if (oldAvatarUrl != null) {
            mediaStorageService.delete(oldAvatarUrl);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserProfileResponse removeAvatar(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String oldAvatarUrl = u.getAvatarUrl();
        u.setAvatarUrl(null);
        User saved = userRepository.save(u);

        if (oldAvatarUrl != null) {
            mediaStorageService.delete(oldAvatarUrl);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteMyAccount(Long userId, String password) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(password, u.getPasswordHash())) {
            throw new BadRequestException("Password is incorrect");
        }

        deleteAccount(userId);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId) {
        User u = userRepository.findById(userId)
                .filter(x -> x.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (u.getRole() == UserRole.ADMIN) {
            throw new ConflictException("Cannot delete an admin account");
        }

        if (u.getAvatarUrl() != null) {
            mediaStorageService.delete(u.getAvatarUrl());
        }

        u.setFullName("O'chirilgan foydalanuvchi");
        u.setEmail("deleted-" + userId + "@deleted.local");
        u.setPhone(null);
        u.setAvatarUrl(null);
        u.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        u.setDeletedAt(Instant.now());
        userRepository.save(u);

        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private UserProfileResponse toResponse(User u) {
        return new UserProfileResponse(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getPhone(),
                u.getAvatarUrl(),
                u.getRole().name(),
                u.getCreatedAt());
    }
}
