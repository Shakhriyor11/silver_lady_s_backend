package com.portfolio.silver_lady_s.dto.about;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AboutResponse {
    private String shopName;
    private String address;
    private String phone;
    private String email;
    private String workingHours;
    private String locationLink;
    private String description;
}
