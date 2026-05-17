package com.portfolio.silver_lady_s.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOtpRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "Invalid phone number format")
    private String phone;
}
