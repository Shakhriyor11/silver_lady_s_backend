package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.sitesettings.SiteSettingsResponse;
import com.portfolio.silver_lady_s.dto.sitesettings.SiteSettingsUpdateRequest;
import com.portfolio.silver_lady_s.service.SiteSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/site-settings")
@RequiredArgsConstructor
public class SiteSettingsController {

    private final SiteSettingsService siteSettingsService;

    @GetMapping
    public SiteSettingsResponse getSettings() {
        return siteSettingsService.getSettings();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SiteSettingsResponse update(@Valid @RequestBody SiteSettingsUpdateRequest dto) {
        return siteSettingsService.update(dto);
    }
}
