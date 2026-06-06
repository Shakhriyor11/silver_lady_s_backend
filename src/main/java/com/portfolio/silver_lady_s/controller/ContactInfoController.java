package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.contactinfo.ContactInfoResponse;
import com.portfolio.silver_lady_s.dto.contactinfo.ContactInfoUpdateRequest;
import com.portfolio.silver_lady_s.service.ContactInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact-info")
@RequiredArgsConstructor
public class ContactInfoController {

    private final ContactInfoService contactInfoService;

    @GetMapping
    public ContactInfoResponse get() {
        return contactInfoService.get();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ContactInfoResponse update(@Valid @RequestBody ContactInfoUpdateRequest dto) {
        return contactInfoService.update(dto);
    }
}
