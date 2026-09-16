package com.portfolio.silver_lady_s.dto.product;

import com.portfolio.silver_lady_s.dto.category.CategoryDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HomeSectionDto {
    private CategoryDto category;
    private List<ProductDto> products;
}
