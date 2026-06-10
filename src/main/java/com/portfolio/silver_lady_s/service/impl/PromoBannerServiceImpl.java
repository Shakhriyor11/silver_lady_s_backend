package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.config.CacheConfig;
import com.portfolio.silver_lady_s.dto.promo.CreatePromoBannerRequest;
import com.portfolio.silver_lady_s.dto.promo.PromoBannerDto;
import com.portfolio.silver_lady_s.dto.promo.UpdatePromoBannerRequest;
import com.portfolio.silver_lady_s.entity.PromoBanner;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.PromoBannerRepository;
import com.portfolio.silver_lady_s.service.MediaStorageService;
import com.portfolio.silver_lady_s.service.PromoBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromoBannerServiceImpl implements PromoBannerService {

    private final PromoBannerRepository promoBannerRepository;
    private final MediaStorageService mediaStorageService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.CACHE_PROMO_BANNERS)
    public List<PromoBannerDto> getActive() {
        return promoBannerRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc().stream()
                .map(PromoBannerDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromoBannerDto> getAll() {
        return promoBannerRepository.findAllByOrderByDisplayOrderAscIdAsc().stream()
                .map(PromoBannerDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PromoBannerDto getById(Long id) {
        return PromoBannerDto.from(findById(id));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_PROMO_BANNERS, allEntries = true)
    public PromoBannerDto create(CreatePromoBannerRequest req, MultipartFile image) {
        PromoBanner banner = new PromoBanner();
        applyRequest(banner, req);
        if (image != null && !image.isEmpty()) {
            banner.setImageUrl(mediaStorageService.storeInFolder(image, "promo"));
        }
        return PromoBannerDto.from(promoBannerRepository.save(banner));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_PROMO_BANNERS, allEntries = true)
    public PromoBannerDto update(Long id, UpdatePromoBannerRequest req, MultipartFile image) {
        PromoBanner banner = findById(id);
        if (req.getTitleUz()    != null) banner.setTitleUz(req.getTitleUz());
        if (req.getTitleRu()    != null) banner.setTitleRu(req.getTitleRu());
        if (req.getTitleEn()    != null) banner.setTitleEn(req.getTitleEn());
        if (req.getSubtitleUz() != null) banner.setSubtitleUz(req.getSubtitleUz());
        if (req.getSubtitleRu() != null) banner.setSubtitleRu(req.getSubtitleRu());
        if (req.getSubtitleEn() != null) banner.setSubtitleEn(req.getSubtitleEn());
        if (req.getDisplayOrder() != null) banner.setDisplayOrder(req.getDisplayOrder());
        if (req.getActive()       != null) banner.setActive(req.getActive());
        if (image != null && !image.isEmpty()) {
            String oldUrl = banner.getImageUrl();
            banner.setImageUrl(mediaStorageService.storeInFolder(image, "promo"));
            if (oldUrl != null) mediaStorageService.delete(oldUrl);
        }
        return PromoBannerDto.from(promoBannerRepository.save(banner));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_PROMO_BANNERS, allEntries = true)
    public void delete(Long id) {
        PromoBanner banner = findById(id);
        if (banner.getImageUrl() != null) mediaStorageService.delete(banner.getImageUrl());
        promoBannerRepository.delete(banner);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_PROMO_BANNERS, allEntries = true)
    public PromoBannerDto setActive(Long id, boolean active) {
        PromoBanner banner = findById(id);
        banner.setActive(active);
        return PromoBannerDto.from(promoBannerRepository.save(banner));
    }

    private PromoBanner findById(Long id) {
        return promoBannerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promo banner not found: id=" + id));
    }

    private void applyRequest(PromoBanner banner, CreatePromoBannerRequest req) {
        banner.setTitleUz(req.getTitleUz());
        banner.setTitleRu(req.getTitleRu());
        banner.setTitleEn(req.getTitleEn());
        banner.setSubtitleUz(req.getSubtitleUz());
        banner.setSubtitleRu(req.getSubtitleRu());
        banner.setSubtitleEn(req.getSubtitleEn());
        banner.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        banner.setActive(req.getActive() != null ? req.getActive() : true);
    }
}
