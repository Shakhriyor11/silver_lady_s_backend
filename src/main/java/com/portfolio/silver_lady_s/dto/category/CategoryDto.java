package com.portfolio.silver_lady_s.dto.category;

import com.portfolio.silver_lady_s.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
    private String nameUz;
    private String nameRu;
    private String nameEn;

    public static CategoryDto from(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getNameUz(), c.getNameRu(), c.getNameEn());
    }
}
