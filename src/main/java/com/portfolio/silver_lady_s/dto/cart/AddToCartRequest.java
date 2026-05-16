package com.portfolio.silver_lady_s.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddToCartRequest {
    @NotNull
    private Long productId;

    @NotNull @Min(1) @Max(99)
    private Integer quantity;
}
