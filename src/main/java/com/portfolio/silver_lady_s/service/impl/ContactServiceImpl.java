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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

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

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private ContactResponse toResponse(ContactMessage msg) {
        Long productId   = msg.getProduct() != null ? msg.getProduct().getId()   : null;
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
                msg.getCreatedAt()
        );
    }
}
