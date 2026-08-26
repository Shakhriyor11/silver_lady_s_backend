package com.portfolio.silver_lady_s.util;

import com.portfolio.silver_lady_s.entity.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public final class PriceCalculator {

    private PriceCalculator() {}

    public static BigDecimal computeSalePrice(Product p) {
        BigDecimal price = p.getPrice();
        Instant now = Instant.now();

        boolean inWindow = (p.getDiscountStartsAt() == null || !now.isBefore(p.getDiscountStartsAt()))
                        && (p.getDiscountEndsAt()   == null || now.isBefore(p.getDiscountEndsAt()));

        if (!inWindow) return price;

        if (p.getDiscountPercent() != null && p.getDiscountPercent() > 0) {
            BigDecimal factor = BigDecimal.ONE.subtract(
                    BigDecimal.valueOf(p.getDiscountPercent()).divide(BigDecimal.valueOf(100)));
            return price.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        }
        if (p.getDiscountAmount() != null && p.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal result = price.subtract(p.getDiscountAmount());
            return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result.setScale(2, RoundingMode.HALF_UP);
        }
        return price;
    }
}
