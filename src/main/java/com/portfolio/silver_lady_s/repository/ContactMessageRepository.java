package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    /**
     * Admin uchun barcha xabarlar — foydalanuvchi va mahsulot ma'lumotlari bilan.
     * Spring Data Page<findAll> ni @EntityGraph bilan to'g'ri override qiladi.
     */
    @EntityGraph(attributePaths = {"user", "product", "product.category"})
    Page<ContactMessage> findAll(Pageable pageable);

    /**
     * Bitta xabarni to'liq ma'lumotlar bilan olish.
     * Nomi oddiy — Spring Data uni to'g'ri tushunadi.
     */
    @EntityGraph(attributePaths = {"user", "product", "product.category"})
    @Query("SELECT m FROM ContactMessage m WHERE m.id = :id")
    Optional<ContactMessage> findByIdWithDetails(@Param("id") Long id);
}
