package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "site_visits",
        indexes = {
                @Index(name = "idx_sv_date",       columnList = "visit_date"),
                @Index(name = "idx_sv_visitor_date", columnList = "visitor_id, visit_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class SiteVisit {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visitor_id", nullable = false, length = 64)
    private String visitorId;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "timestamp(6) with time zone not null default now()")
    private Instant createdAt;
}
