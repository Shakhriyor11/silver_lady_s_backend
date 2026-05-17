package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.contact.ContactRequest;
import com.portfolio.silver_lady_s.dto.contact.ContactResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactService {
    /** Foydalanuvchi sotuvchiga xabar yuboradi */
    ContactResponse send(Long userId, ContactRequest req);

    /** Admin barcha xabarlarni ko'radi */
    Page<ContactResponse> listAll(Pageable pageable);

    /** Admin bitta xabarni ko'radi */
    ContactResponse getById(Long id);

    /** Admin xabarni o'qilgan deb belgilaydi */
    ContactResponse markRead(Long id);
}
