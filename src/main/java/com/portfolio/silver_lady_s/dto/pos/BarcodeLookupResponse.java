package com.portfolio.silver_lady_s.dto.pos;

import com.portfolio.silver_lady_s.dto.product.SizeEntryDto;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.util.PriceCalculator;

import java.math.BigDecimal;
import java.util.List;

public record BarcodeLookupResponse(
        Long productId,
        String barcode,
        String name,
        BigDecimal price,
        BigDecimal salePrice,
        List<SizeEntryDto> sizeEntries,
        boolean active
) {
    public static BarcodeLookupResponse from(Product p) {
        return new BarcodeLookupResponse(
                p.getId(),
                p.getBarcode(),
                p.getName(),
                p.getPrice(),
                PriceCalculator.computeSalePrice(p),
                p.getSizeEntries().stream().map(SizeEntryDto::from).toList(),
                p.isActive());
    }
}
