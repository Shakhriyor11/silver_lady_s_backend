package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.contact.ContactRequest;
import com.portfolio.silver_lady_s.dto.contact.ContactResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactService {
    ContactResponse send(Long userId, ContactRequest req);
    Page<ContactResponse> listAll(Pageable pageable);
    ContactResponse getById(Long id);
    ContactResponse markRead(Long id);
    ContactResponse reply(Long messageId, String replyText);
    Page<ContactResponse> listByUser(Long userId, Pageable pageable);
    ContactResponse sendToUser(Long targetUserId, String subject, String message);
}
