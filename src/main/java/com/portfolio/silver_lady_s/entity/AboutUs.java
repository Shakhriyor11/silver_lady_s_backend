package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "about_us")
@Getter
@Setter
@NoArgsConstructor
public class AboutUs extends BaseTimeEntity {
@Getter
@Setter
@NoArgsConstructor
public class AboutUs {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "singleton_key", nullable = false, unique = true, updatable = false,
            columnDefinition = "integer default 1 check (singleton_key = 1)")
    private int singletonKey = 1;

    @Column(nullable = false, length = 120)
    private String shopName;

    @Column(length = 120) private String shopNameUz;
    @Column(length = 120) private String shopNameRu;
    @Column(length = 120) private String shopNameEn;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 255) private String addressUz;
    @Column(length = 255) private String addressRu;
    @Column(length = 255) private String addressEn;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(nullable = false, length = 80)
    private String workingHours;

    @Column(length = 80) private String workingHoursUz;
    @Column(length = 80) private String workingHoursRu;
    @Column(length = 80) private String workingHoursEn;

    @Column(length = 500)
    private String locationLink;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT") private String descriptionUz;
    @Column(columnDefinition = "TEXT") private String descriptionRu;
    @Column(columnDefinition = "TEXT") private String descriptionEn;
}
