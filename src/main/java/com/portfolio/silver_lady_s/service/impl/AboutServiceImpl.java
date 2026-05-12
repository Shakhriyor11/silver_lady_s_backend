package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.config.CacheConfig;
import com.portfolio.silver_lady_s.dto.about.AboutResponse;
import com.portfolio.silver_lady_s.dto.about.AboutUpdateRequest;
import com.portfolio.silver_lady_s.entity.AboutUs;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.AboutUsRepository;
import com.portfolio.silver_lady_s.service.AboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements AboutService {

    private final AboutUsRepository aboutUsRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.CACHE_ABOUT)
    public AboutResponse getAboutInfo() {
        AboutUs aboutUs = aboutUsRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException("About info not found"));
        return toResponse(aboutUs);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_ABOUT, allEntries = true)
    public AboutResponse update(AboutUpdateRequest dto) {
        AboutUs aboutUs = aboutUsRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException("About info not found"));

        aboutUs.setShopName(dto.getShopName().trim());
        aboutUs.setAddress(dto.getAddress().trim());
        aboutUs.setPhone(dto.getPhone().trim());
        aboutUs.setEmail(dto.getEmail() == null ? null : dto.getEmail().trim());
        aboutUs.setLocationLink(dto.getLocationLink() == null ? null : dto.getLocationLink().trim());
        aboutUs.setWorkingHours(dto.getWorkingHours().trim());
        aboutUs.setDescription(dto.getDescription());

        return toResponse(aboutUsRepository.save(aboutUs));
    }

    private AboutResponse toResponse(AboutUs a) {
        return new AboutResponse(
                a.getShopName(),
                a.getAddress(),
                a.getPhone(),
                a.getEmail(),
                a.getWorkingHours(),
                a.getLocationLink(),
                a.getDescription()
        );
    }
}
