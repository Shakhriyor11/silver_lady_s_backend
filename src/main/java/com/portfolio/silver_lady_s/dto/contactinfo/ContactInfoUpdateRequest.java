package com.portfolio.silver_lady_s.dto.contactinfo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactInfoUpdateRequest {

    @NotBlank @Size(max = 40)
    private String phone;

    @Size(max = 40)
    private String phone2;

    @Email @Size(max = 120)
    private String email;

    @NotBlank @Size(max = 255)
    private String address;

    @Size(max = 255) private String addressUz;
    @Size(max = 255) private String addressRu;
    @Size(max = 255) private String addressEn;

    @NotBlank @Size(max = 80)
    private String workingHours;

    @Size(max = 80) private String workingHoursUz;
    @Size(max = 80) private String workingHoursRu;
    @Size(max = 80) private String workingHoursEn;

    @Size(max = 500)
    private String locationLink;

    @Size(max = 255)
    private String instagram;

    @Size(max = 255)
    private String telegram;

    @Size(max = 40)
    private String whatsapp;

    @Size(max = 255)
    private String facebook;

    @Size(max = 255)
    private String youtube;
}
