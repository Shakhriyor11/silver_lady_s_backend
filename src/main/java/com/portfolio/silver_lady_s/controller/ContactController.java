package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.contact.AdminMessageRequest;
import com.portfolio.silver_lady_s.dto.contact.ContactRequest;
import com.portfolio.silver_lady_s.dto.contact.ContactResponse;
import com.portfolio.silver_lady_s.dto.contact.ReplyRequest;
import com.portfolio.silver_lady_s.security.CurrentUser;
import com.portfolio.silver_lady_s.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Sotuvchi (do'kon) bilan aloqa so'rovlari.
 *
 * POST /api/contact         — autentifikatsiyadan o'tgan foydalanuvchi xabar yuboradi.
 * GET  /api/contact         — admin barcha xabarlarni ko'radi (sahifalash bilan).
 * GET  /api/contact/{id}    — admin bitta xabarni ko'radi.
 * PATCH /api/contact/{id}/read — admin xabarni o'qilgan deb belgilaydi.
 */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse send(@Valid @RequestBody ContactRequest req) {
        Long userId = CurrentUser.principal().getUserId();
        return contactService.send(userId, req);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ContactResponse> listAll(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return contactService.listAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ContactResponse getById(@PathVariable Long id) {
        return contactService.getById(id);
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ContactResponse markRead(@PathVariable Long id) {
        return contactService.markRead(id);
    }

    @PostMapping("/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ContactResponse reply(@PathVariable Long id, @Valid @RequestBody ReplyRequest req) {
        return contactService.reply(id, req.getReply());
    }

    @GetMapping("/mine")
    public Page<ContactResponse> myMessages(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Long userId = CurrentUser.principal().getUserId();
        return contactService.listByUser(userId, pageable);
    }

    @PostMapping("/admin/send")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse sendToUser(@Valid @RequestBody AdminMessageRequest req) {
        return contactService.sendToUser(req.getTargetUserId(), req.getSubject(), req.getMessage());
    }
}
