package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.config.CacheConfig;
import com.portfolio.silver_lady_s.dto.sitesettings.SiteSettingsResponse;
import com.portfolio.silver_lady_s.dto.sitesettings.SiteSettingsUpdateRequest;
import com.portfolio.silver_lady_s.entity.SiteSettings;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.SiteSettingsRepository;
import com.portfolio.silver_lady_s.service.SiteSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SiteSettingsServiceImpl implements SiteSettingsService {

    private final SiteSettingsRepository siteSettingsRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.CACHE_SITE_SETTINGS)
    public SiteSettingsResponse getSettings() {
        SiteSettings s = siteSettingsRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException("Site settings not found"));
        return SiteSettingsResponse.from(s);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_SITE_SETTINGS, allEntries = true)
    public SiteSettingsResponse update(SiteSettingsUpdateRequest dto) {
        SiteSettings s = siteSettingsRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException("Site settings not found"));

        s.setLeavesEffectEnabled(dto.getLeavesEffectEnabled());

        return SiteSettingsResponse.from(siteSettingsRepository.save(s));
    }
}
