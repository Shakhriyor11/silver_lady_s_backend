package com.portfolio.silver_lady_s.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailOtpRequest {

    @Email
    @NotBlank
    @Size(max = 120)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{4,8}$", message = "OTP must be 4-8 digits")
    private String otp;
}
