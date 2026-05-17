package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Barcha entity'lar uchun umumiy audit timestamp'lar.
 * createdAt — yozuv birinchi saqlanganda bir marta o'rnatiladi.
 * updatedAt — har yangilashda avtomatik o'zgaradi.
 */
@MappedSuperclass
@Getter
public abstract class BaseTimeEntity {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
