package com.portfolio.silver_lady_s.dto.product;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class UpdateProductRequest {

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

    @Min(0)
    private Integer stockQuantity;

    @Size(max = 64)
    private String barcode;

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

    @Size(max = 20)
    private List<SizeEntryRequest> sizeEntries;

    private Boolean active;

    @AssertTrue(message = "discountEndsAt must be after discountStartsAt")
    private boolean isDiscountDatesValid() {
        if (discountStartsAt == null || discountEndsAt == null) return true;
        return discountEndsAt.isAfter(discountStartsAt);
    }
}
