package com.portfolio.silver_lady_s.dto.carousel;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCarouselItemRequest {

    @Size(max = 200) private String titleUz;
    @Size(max = 200) private String titleRu;
    @Size(max = 200) private String titleEn;

    @Size(max = 400) private String subtitleUz;
    @Size(max = 400) private String subtitleRu;
    @Size(max = 400) private String subtitleEn;

    @Size(max = 500)
    private String link;

    @Min(0)
    private Integer displayOrder;

    private Boolean active;
}
