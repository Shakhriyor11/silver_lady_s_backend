package com.portfolio.silver_lady_s.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SizeEntryRequest(
        @NotBlank @Size(max = 30) String size,
        @Min(0) int quantity
) {}
