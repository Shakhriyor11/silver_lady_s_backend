package com.portfolio.silver_lady_s.dto.about;

import com.portfolio.silver_lady_s.entity.AboutUs;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AboutResponse {
    private String shopName;
    private String shopNameUz;
    private String shopNameRu;
    private String shopNameEn;

    private String address;
    private String addressUz;
    private String addressRu;
    private String addressEn;

    private String phone;
    private String email;

    private String workingHours;
    private String workingHoursUz;
    private String workingHoursRu;
    private String workingHoursEn;

    private String locationLink;

    private String description;
    private String descriptionUz;
    private String descriptionRu;
    private String descriptionEn;

    public static AboutResponse from(AboutUs a) {
        return new AboutResponse(
                a.getShopName(), a.getShopNameUz(), a.getShopNameRu(), a.getShopNameEn(),
                a.getAddress(), a.getAddressUz(), a.getAddressRu(), a.getAddressEn(),
                a.getPhone(), a.getEmail(),
                a.getWorkingHours(), a.getWorkingHoursUz(), a.getWorkingHoursRu(), a.getWorkingHoursEn(),
                a.getLocationLink(),
                a.getDescription(), a.getDescriptionUz(), a.getDescriptionRu(), a.getDescriptionEn()
        );
    }
}