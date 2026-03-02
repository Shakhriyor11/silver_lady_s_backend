package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.user.UpdateProfileRequest;
import com.portfolio.silver_lady_s.dto.user.UserProfileResponse;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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

    private UserProfileResponse toResponse(User u) {
        return new UserProfileResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole().name());
    }
}
