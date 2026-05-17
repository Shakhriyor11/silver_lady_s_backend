package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.carousel.CarouselItemDto;
import com.portfolio.silver_lady_s.dto.carousel.CreateCarouselItemRequest;
import com.portfolio.silver_lady_s.dto.carousel.UpdateCarouselItemRequest;
import com.portfolio.silver_lady_s.service.CarouselService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carousel")
@RequiredArgsConstructor
public class CarouselController {

    private final CarouselService carouselService;

    @GetMapping
    public List<CarouselItemDto> getActive() {
        return carouselService.getActive();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CarouselItemDto> getAll() {
        return carouselService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CarouselItemDto getById(@PathVariable Long id) {
        return carouselService.getById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarouselItemDto> create(
            @RequestPart("data") @Valid CreateCarouselItemRequest req,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        CarouselItemDto created = carouselService.create(req, image);
        return ResponseEntity.created(URI.create("/api/carousel/" + created.getId())).body(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public CarouselItemDto update(
            @PathVariable Long id,
            @RequestPart("data") @Valid UpdateCarouselItemRequest req,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return carouselService.update(id, req, image);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carouselService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public CarouselItemDto setActive(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return carouselService.setActive(id, active);
    }
}
