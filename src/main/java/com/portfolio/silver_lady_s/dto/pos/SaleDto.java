package com.portfolio.silver_lady_s.dto.pos;

import com.portfolio.silver_lady_s.entity.Sale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleDto(
        Long id,
        Long cashierId,
        String cashierName,
        BigDecimal totalAmount,
        List<SaleItemDto> items,
        Instant createdAt
) {
    public static SaleDto from(Sale s) {
        return new SaleDto(
                s.getId(),
                s.getCashier().getId(),
                s.getCashier().getFullName(),
                s.getTotalAmount(),
                s.getItems().stream().map(SaleItemDto::from).toList(),
                s.getCreatedAt());
    }
}
