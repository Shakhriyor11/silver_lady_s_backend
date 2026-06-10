package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.promo.CreatePromoBannerRequest;
import com.portfolio.silver_lady_s.dto.promo.PromoBannerDto;
import com.portfolio.silver_lady_s.dto.promo.UpdatePromoBannerRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PromoBannerService {

    List<PromoBannerDto> getActive();

    List<PromoBannerDto> getAll();

    PromoBannerDto getById(Long id);

    PromoBannerDto create(CreatePromoBannerRequest req, MultipartFile image);

    PromoBannerDto update(Long id, UpdatePromoBannerRequest req, MultipartFile image);

    void delete(Long id);

    PromoBannerDto setActive(Long id, boolean active);
}
