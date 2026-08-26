package com.portfolio.silver_lady_s.dto.pos;

import com.portfolio.silver_lady_s.entity.SaleItem;

import java.math.BigDecimal;

public record SaleItemDto(
        Long productId,
        String productName,
        String size,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
    public static SaleItemDto from(SaleItem i) {
        return new SaleItemDto(
                i.getProduct() != null ? i.getProduct().getId() : null,
                i.getProductName(),
                i.getSize(),
                i.getUnitPrice(),
                i.getQuantity(),
                i.getLineTotal());
    }
}
