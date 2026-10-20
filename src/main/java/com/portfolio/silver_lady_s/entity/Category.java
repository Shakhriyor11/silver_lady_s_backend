package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "categories",
        indexes = @Index(name = "idx_category_name", columnList = "name")
)
@Getter
@Setter
@NoArgsConstructor
public class Category extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 80) private String nameUz;
    @Column(length = 80) private String nameRu;
    @Column(length = 80) private String nameEn;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "show_size_filter", nullable = false)
    private boolean showSizeFilter = false;

    /** SEO-friendly URL bo'lagi, nom + id dan avtomatik yasaladi, bir marta o'rnatilib keyin o'zgarmaydi */
    @Column(unique = true, length = 120)
    private String slug;

    @Column(name = "seo_title", length = 160)
    private String seoTitle;

    @Column(name = "seo_description", length = 300)
    private String seoDescription;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** SEO-friendly URL bo'lagi, nom + id dan avtomatik yasaladi, bir marta o'rnatilib keyin o'zgarmaydi */
    @Column(unique = true, length = 120)
    private String slug;

    @Column(name = "seo_title", length = 160)
    private String seoTitle;

    @Column(name = "seo_description", length = 300)
    private String seoDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, id ASC")
    private List<Category> children = new ArrayList<>();
}
