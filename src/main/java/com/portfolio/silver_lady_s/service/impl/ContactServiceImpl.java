package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.contact.ContactRequest;
import com.portfolio.silver_lady_s.dto.contact.ContactResponse;
import com.portfolio.silver_lady_s.entity.ContactMessage;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.ContactMessageRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.service.ContactService;
import com.portfolio.silver_lady_s.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public ContactResponse send(Long userId, ContactRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));

        Product product = null;
        if (req.getProductId() != null) {
            product = productRepository.findByIdAndActiveTrue(req.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: id=" + req.getProductId()));
        }

        ContactMessage msg = new ContactMessage();
        msg.setUser(user);
        msg.setProduct(product);
        msg.setSubject(req.getSubject().trim());
        msg.setMessage(req.getMessage().trim());
        msg.setRead(false);
        msg.setAdminInitiated(false);

        return toResponse(contactMessageRepository.save(msg));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> listAll(Pageable pageable) {
        return contactMessageRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponse getById(Long id) {
        ContactMessage msg = contactMessageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Contact message not found: id=" + id));
        return toResponse(msg);
    }

    @Override
    @Transactional
    public ContactResponse markRead(Long id) {
        ContactMessage msg = contactMessageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Contact message not found: id=" + id));
        msg.setRead(true);
        return toResponse(contactMessageRepository.save(msg));
    }

    @Override
    @Transactional
    public ContactResponse reply(Long messageId, String replyText) {
        ContactMessage msg = contactMessageRepository.findByIdWithDetails(messageId)
                .orElseThrow(() -> new NotFoundException("Contact message not found: id=" + messageId));
        msg.setAdminReply(replyText.trim());
        msg.setRepliedAt(Instant.now());
        msg.setRead(true);
        ContactResponse saved = toResponse(contactMessageRepository.save(msg));

        emailService.sendReplyNotification(
                msg.getUser().getEmail(),
                msg.getUser().getFullName(),
                msg.getSubject(),
                replyText.trim()
        );
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> listByUser(Long userId, Pageable pageable) {
        return contactMessageRepository.findByUserId(userId, pageable).map(this::toResponseForUser);
    }

    @Override
    @Transactional
    public ContactResponse sendToUser(Long targetUserId, String subject, String message) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + targetUserId));

        ContactMessage msg = new ContactMessage();
        msg.setUser(user);
        msg.setSubject(subject.trim());
        msg.setMessage(message.trim());
        msg.setAdminInitiated(true);
        msg.setRead(true);

        ContactResponse saved = toResponseForUser(contactMessageRepository.save(msg));

        emailService.sendAdminMessage(
                user.getEmail(),
                user.getFullName(),
                subject.trim(),
                message.trim()
        );
        return saved;
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private ContactResponse toResponse(ContactMessage msg) {
        Long productId     = msg.getProduct() != null ? msg.getProduct().getId()   : null;
        String productName = msg.getProduct() != null ? msg.getProduct().getName() : null;
        return new ContactResponse(
                msg.getId(),
                msg.getUser().getId(),
                msg.getUser().getFullName(),
                msg.getUser().getEmail(),
                productId,
                productName,
                msg.getSubject(),
                msg.getMessage(),
                msg.isRead(),
                msg.isAdminInitiated(),
                msg.getAdminReply(),
                msg.getRepliedAt(),
                msg.getCreatedAt()
        );
    }

    private ContactResponse toResponseForUser(ContactMessage msg) {
        Long productId     = msg.getProduct() != null ? msg.getProduct().getId()   : null;
        String productName = msg.getProduct() != null ? msg.getProduct().getName() : null;
        return new ContactResponse(
                msg.getId(),
                null, null, null,
                productId,
                productName,
                msg.getSubject(),
                msg.getMessage(),
                msg.isRead(),
                msg.isAdminInitiated(),
                msg.getAdminReply(),
                msg.getRepliedAt(),
                msg.getCreatedAt()
        );
    }
}
