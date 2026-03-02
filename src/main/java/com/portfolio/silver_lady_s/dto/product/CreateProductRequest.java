package com.portfolio.silver_lady_s.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CreateProductRequest {
    @NotBlank @Size(max = 160)
    private String name;

    @Size(max = 10000)
    private String description;

    @NotNull @Positive
    private BigDecimal price;

    @NotNull
    private Long categoryId;

    private Boolean active;
}
