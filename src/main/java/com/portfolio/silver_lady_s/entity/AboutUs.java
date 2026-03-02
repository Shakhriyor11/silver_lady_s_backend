package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "about_us")
@Getter @Setter @NoArgsConstructor
public class AboutUs {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String shopName;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(nullable = false, length = 80)
    private String workingHours;

    @Column(length = 500)
    private String locationLink;

    @Column(columnDefinition = "TEXT")
    private String description;
}
