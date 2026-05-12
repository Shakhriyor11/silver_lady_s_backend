package com.portfolio.silver_lady_s.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {

    @NotBlank(message = "Shipping address is required")
    @Size(max = 500)
    private String shippingAddress;

    @Size(max = 40)
    private String phone;
}
