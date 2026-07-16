package com.portfolio.silver_lady_s.dto.product;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class CategoryDiscountRequest {

    @Min(0)
    @Max(100)
    private Integer discountPercent;

    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal discountAmount;

    // null — cheklovsiz (darhol boshlanadi / hech qachon tugamaydi)
    private Instant discountStartsAt;
    private Instant discountEndsAt;

    @AssertTrue(message = "discountPercent va discountAmount bir vaqtda kiritilmasin — faqat bittasini tanlang")
    private boolean isOnlyOneDiscountType() {
        return discountPercent == null || discountAmount == null;
    }

    @AssertTrue(message = "discountEndsAt discountStartsAt dan keyin bo'lishi kerak")
    private boolean isValidTimeWindow() {
        return discountStartsAt == null || discountEndsAt == null || discountEndsAt.isAfter(discountStartsAt);
    }
}

