package com.portfolio.silver_lady_s.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank @Size(max = 120)
    private String fullName;

    @Email @NotBlank @Size(max = 120)
    private String email;

    @NotBlank @Size(min = 6, max = 72)
    private String password;

    @Size(max = 40)
    private String phone;
}
