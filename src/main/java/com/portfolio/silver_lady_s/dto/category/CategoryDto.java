package com.portfolio.silver_lady_s.dto.category;

import com.portfolio.silver_lady_s.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
    private String nameUz;
    private String nameRu;
    private String nameEn;
    private int sortOrder;
    private boolean showSizeFilter;
    private Long parentId;
    private List<CategoryDto> children;

    public static CategoryDto from(Category c) {
        List<CategoryDto> kids = c.getChildren().stream()
                .map(CategoryDto::from)
                .toList();
        return new CategoryDto(
                c.getId(),
                c.getName(),
                c.getNameUz(),
                c.getNameRu(),
                c.getNameEn(),
                c.getSortOrder(),
                c.isShowSizeFilter(),
                c.getParent() != null ? c.getParent().getId() : null,
                kids
        );
    }
}
