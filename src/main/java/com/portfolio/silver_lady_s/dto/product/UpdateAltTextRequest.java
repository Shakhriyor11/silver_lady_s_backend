package com.portfolio.silver_lady_s.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAltTextRequest {

    @NotBlank @Size(max = 255)
    private String altText;
}
