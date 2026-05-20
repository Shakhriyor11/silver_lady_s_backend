package com.portfolio.silver_lady_s.dto.order;

import com.portfolio.silver_lady_s.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemDto(
        Long productId,
        String productName,
        String selectedSize,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
    public static OrderItemDto from(OrderItem item) {
        return new OrderItemDto(
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getProductName(),
                item.getSelectedSize(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}
