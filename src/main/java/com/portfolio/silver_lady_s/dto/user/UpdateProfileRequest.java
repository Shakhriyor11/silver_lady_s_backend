package com.portfolio.silver_lady_s.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequest {
    @NotBlank @Size(max = 120)
    private String fullName;

    @Size(max = 40)
    private String phone;
}
