package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contact_info")
@Getter
@Setter
@NoArgsConstructor
public class ContactInfo extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "singleton_key", nullable = false, unique = true, updatable = false,
            columnDefinition = "integer default 1 check (singleton_key = 1)")
    private int singletonKey = 1;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(length = 40)
    private String phone2;

    @Column(length = 120)
    private String email;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 255) private String addressUz;
    @Column(length = 255) private String addressRu;
    @Column(length = 255) private String addressEn;

    @Column(nullable = false, length = 80)
    private String workingHours;

    @Column(length = 80) private String workingHoursUz;
    @Column(length = 80) private String workingHoursRu;
    @Column(length = 80) private String workingHoursEn;

    @Column(length = 500)
    private String locationLink;

    @Column(length = 255)
    private String instagram;

    @Column(length = 255)
    private String telegram;

    @Column(length = 40)
    private String whatsapp;

    @Column(length = 255)
    private String facebook;

    @Column(length = 255)
    private String youtube;
}
