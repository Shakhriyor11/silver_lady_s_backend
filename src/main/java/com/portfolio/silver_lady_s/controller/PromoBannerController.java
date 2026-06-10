package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.promo.CreatePromoBannerRequest;
import com.portfolio.silver_lady_s.dto.promo.PromoBannerDto;
import com.portfolio.silver_lady_s.dto.promo.UpdatePromoBannerRequest;
import com.portfolio.silver_lady_s.service.PromoBannerService;
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
@RequestMapping("/api/promo-banners")
@RequiredArgsConstructor
public class PromoBannerController {

    private final PromoBannerService promoBannerService;

    @GetMapping
    public List<PromoBannerDto> getActive() {
        return promoBannerService.getActive();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PromoBannerDto> getAll() {
        return promoBannerService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PromoBannerDto getById(@PathVariable Long id) {
        return promoBannerService.getById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromoBannerDto> create(
            @RequestPart("data") @Valid CreatePromoBannerRequest req,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        PromoBannerDto created = promoBannerService.create(req, image);
        return ResponseEntity.created(URI.create("/api/promo-banners/" + created.getId())).body(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public PromoBannerDto update(
            @PathVariable Long id,
            @RequestPart("data") @Valid UpdatePromoBannerRequest req,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return promoBannerService.update(id, req, image);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promoBannerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public PromoBannerDto setActive(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return promoBannerService.setActive(id, active);
    }
}
