package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.carousel.CarouselItemDto;
import com.portfolio.silver_lady_s.dto.carousel.CreateCarouselItemRequest;
import com.portfolio.silver_lady_s.dto.carousel.UpdateCarouselItemRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CarouselService {

    List<CarouselItemDto> getActive();

    List<CarouselItemDto> getAll();

    CarouselItemDto getById(Long id);

    CarouselItemDto create(CreateCarouselItemRequest req, MultipartFile image);

    CarouselItemDto update(Long id, UpdateCarouselItemRequest req, MultipartFile image);

    void delete(Long id);

    CarouselItemDto setActive(Long id, boolean active);
}
