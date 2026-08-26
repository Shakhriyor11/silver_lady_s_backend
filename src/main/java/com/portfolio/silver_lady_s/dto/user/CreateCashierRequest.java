package com.portfolio.silver_lady_s.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCashierRequest {

    @NotBlank @Email @Size(max = 120)
    private String email;

    @NotBlank @Size(min = 8, max = 100)
    private String password;

    @NotBlank @Size(max = 120)
    private String fullName;
}
