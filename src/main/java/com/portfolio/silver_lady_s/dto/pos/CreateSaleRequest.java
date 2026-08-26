package com.portfolio.silver_lady_s.dto.pos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateSaleRequest {

    @NotEmpty @Size(max = 50) @Valid
    private List<SaleItemRequest> items;
}
