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
        AboutUs a = aboutUsRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException("About info not found"));
        return AboutResponse.from(a);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_ABOUT, allEntries = true)
    public AboutResponse update(AboutUpdateRequest dto) {
        AboutUs a = aboutUsRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException("About info not found"));

        a.setShopName(dto.getShopName().trim());
        a.setShopNameUz(dto.getShopNameUz());
        a.setShopNameRu(dto.getShopNameRu());
        a.setShopNameEn(dto.getShopNameEn());

        a.setAddress(dto.getAddress().trim());
        a.setAddressUz(dto.getAddressUz());
        a.setAddressRu(dto.getAddressRu());
        a.setAddressEn(dto.getAddressEn());

        a.setPhone(dto.getPhone().trim());
        a.setEmail(dto.getEmail() == null ? null : dto.getEmail().trim());

        a.setWorkingHours(dto.getWorkingHours().trim());
        a.setWorkingHoursUz(dto.getWorkingHoursUz());
        a.setWorkingHoursRu(dto.getWorkingHoursRu());
        a.setWorkingHoursEn(dto.getWorkingHoursEn());

        a.setLocationLink(dto.getLocationLink() == null ? null : dto.getLocationLink().trim());

        a.setDescription(dto.getDescription());
        a.setDescriptionUz(dto.getDescriptionUz());
        a.setDescriptionRu(dto.getDescriptionRu());
        a.setDescriptionEn(dto.getDescriptionEn());

        return AboutResponse.from(aboutUsRepository.save(a));
    }
}
