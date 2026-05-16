package com.portfolio.silver_lady_s.dto.product;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank @Size(max = 160)
    private String name;

    @Size(max = 160) private String nameUz;
    @Size(max = 160) private String nameRu;
    @Size(max = 160) private String nameEn;

    @Size(max = 10000)
    private String description;

    @Size(max = 10000) private String descriptionUz;
    @Size(max = 10000) private String descriptionRu;
    @Size(max = 10000) private String descriptionEn;

    @NotNull @Positive
    private BigDecimal price;

    @Min(0) @Max(100)
    private Integer discountPercent;

    @DecimalMin("0.00")
    private BigDecimal discountAmount;

    private Instant discountStartsAt;
    private Instant discountEndsAt;

    @NotEmpty @Size(max = 5)
    private List<Long> categoryIds;

    private Boolean active;
}
