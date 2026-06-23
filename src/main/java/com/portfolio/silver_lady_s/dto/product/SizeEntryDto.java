package com.portfolio.silver_lady_s.dto.product;

import com.portfolio.silver_lady_s.entity.ProductSizeEntry;

public record SizeEntryDto(String size, int quantity) {

    public static SizeEntryDto from(ProductSizeEntry e) {
        return new SizeEntryDto(e.getSize(), e.getQuantity());
    }
}
