package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.config.CacheConfig;
import com.portfolio.silver_lady_s.dto.carousel.CarouselItemDto;
import com.portfolio.silver_lady_s.dto.carousel.CreateCarouselItemRequest;
import com.portfolio.silver_lady_s.dto.carousel.UpdateCarouselItemRequest;
import com.portfolio.silver_lady_s.entity.CarouselItem;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.CarouselItemRepository;
import com.portfolio.silver_lady_s.service.CarouselService;
import com.portfolio.silver_lady_s.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarouselServiceImpl implements CarouselService {

    private final CarouselItemRepository carouselItemRepository;
    private final MediaStorageService mediaStorageService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.CACHE_CAROUSEL)
    public List<CarouselItemDto> getActive() {
        return carouselItemRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc().stream()
                .map(CarouselItemDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarouselItemDto> getAll() {
        return carouselItemRepository.findAllByOrderByDisplayOrderAscIdAsc().stream()
                .map(CarouselItemDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CarouselItemDto getById(Long id) {
        return CarouselItemDto.from(findById(id));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CAROUSEL, allEntries = true)
    public CarouselItemDto create(CreateCarouselItemRequest req, MultipartFile image) {
        CarouselItem item = new CarouselItem();
        applyRequest(item, req.getTitleUz(), req.getTitleRu(), req.getTitleEn(),
                req.getSubtitleUz(), req.getSubtitleRu(), req.getSubtitleEn(),
                req.getLink(),
                req.getDisplayOrder() != null ? req.getDisplayOrder() : 0,
                req.getActive() != null ? req.getActive() : true);

        if (image != null && !image.isEmpty()) {
            item.setImageUrl(mediaStorageService.storeInFolder(image, "carousel"));
        }

        return CarouselItemDto.from(carouselItemRepository.save(item));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CAROUSEL, allEntries = true)
    public CarouselItemDto update(Long id, UpdateCarouselItemRequest req, MultipartFile image) {
        CarouselItem item = findById(id);

        if (req.getTitleUz()    != null) item.setTitleUz(req.getTitleUz());
        if (req.getTitleRu()    != null) item.setTitleRu(req.getTitleRu());
        if (req.getTitleEn()    != null) item.setTitleEn(req.getTitleEn());
        if (req.getSubtitleUz() != null) item.setSubtitleUz(req.getSubtitleUz());
        if (req.getSubtitleRu() != null) item.setSubtitleRu(req.getSubtitleRu());
        if (req.getSubtitleEn() != null) item.setSubtitleEn(req.getSubtitleEn());
        if (req.getLink()          != null) item.setLink(req.getLink());
        if (req.getDisplayOrder()  != null) item.setDisplayOrder(req.getDisplayOrder());
        if (req.getActive()        != null) item.setActive(req.getActive());

        if (image != null && !image.isEmpty()) {
            String oldUrl = item.getImageUrl();
            item.setImageUrl(mediaStorageService.storeInFolder(image, "carousel"));
            if (oldUrl != null) mediaStorageService.delete(oldUrl);
        }

        return CarouselItemDto.from(carouselItemRepository.save(item));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CAROUSEL, allEntries = true)
    public void delete(Long id) {
        CarouselItem item = findById(id);
        if (item.getImageUrl() != null) mediaStorageService.delete(item.getImageUrl());
        carouselItemRepository.delete(item);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CAROUSEL, allEntries = true)
    public CarouselItemDto setActive(Long id, boolean active) {
        CarouselItem item = findById(id);
        item.setActive(active);
        return CarouselItemDto.from(carouselItemRepository.save(item));
    }

    // ─────────────────────────────────────────────────────────────────────────

    private CarouselItem findById(Long id) {
        return carouselItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Carousel item not found: id=" + id));
    }

    private void applyRequest(CarouselItem item,
                               String titleUz, String titleRu, String titleEn,
                               String subtitleUz, String subtitleRu, String subtitleEn,
                               String link, int displayOrder, boolean active) {
        item.setTitleUz(titleUz);
        item.setTitleRu(titleRu);
        item.setTitleEn(titleEn);
        item.setSubtitleUz(subtitleUz);
        item.setSubtitleRu(subtitleRu);
        item.setSubtitleEn(subtitleEn);
        item.setLink(link);
        item.setDisplayOrder(displayOrder);
        item.setActive(active);
    }
}
