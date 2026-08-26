package com.portfolio.silver_lady_s.dto.pos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaleItemRequest(
        @NotBlank @Size(max = 64) String barcode,
        @NotBlank @Size(max = 30) String size,
        @Min(1) int quantity
) {}
