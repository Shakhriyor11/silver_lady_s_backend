package com.portfolio.silver_lady_s.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartItemRequest {
    @NotNull
    private Long productId;

    @Size(max = 30)
    private String selectedSize;

    @NotNull @Min(1) @Max(99)
    private Integer quantity;
}
