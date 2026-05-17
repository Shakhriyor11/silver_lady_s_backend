package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Foydalanuvchi sotuvchiga yuborgan aloqa so'rovi.
 * product bog'lanishi ixtiyoriy — foydalanuvchi ma'lum bir mahsulot haqida
 * savol berishi yoki umumiy xabar yuborishi mumkin.
 */
@Entity
@Table(
        name = "contact_messages",
        indexes = {
                @Index(name = "idx_contact_user_id",    columnList = "user_id"),
                @Index(name = "idx_contact_created_at", columnList = "created_at"),
                @Index(name = "idx_contact_is_read",    columnList = "is_read")
        }
)
@Getter @Setter @NoArgsConstructor
public class ContactMessage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Xabar yuborgan foydalanuvchi */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_contact_user"))
    private User user;

    /** Bog'liq mahsulot (ixtiyoriy) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",
            foreignKey = @ForeignKey(name = "fk_contact_product"))
    private Product product;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Admin tomonidan o'qilgan/o'qilmagan */
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
