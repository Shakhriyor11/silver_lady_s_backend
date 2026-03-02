package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.about.AboutResponse;
import com.portfolio.silver_lady_s.dto.about.AboutUpdateRequest;
import com.portfolio.silver_lady_s.service.AboutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/about")
@RequiredArgsConstructor
public class AboutController {

    private final AboutService aboutService;

    @GetMapping
    public AboutResponse getAbout() {
        return aboutService.getAboutInfo();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AboutResponse update(@Valid @RequestBody AboutUpdateRequest dto) {
        return aboutService.update(dto);
    }
}
