package com.portfolio.silver_lady_s.dto.about;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AboutUpdateRequest {
    @NotBlank @Size(max = 120)
    private String shopName;

    @NotBlank @Size(max = 255)
    private String address;

    @NotBlank @Size(max = 40)
    private String phone;

    @Email @Size(max = 120)
    private String email;

    @NotBlank @Size(max = 80)
    private String workingHours;

    @Size(max = 500)
    private String locationLink;

    @Size(max = 5000)
    private String description;
}
