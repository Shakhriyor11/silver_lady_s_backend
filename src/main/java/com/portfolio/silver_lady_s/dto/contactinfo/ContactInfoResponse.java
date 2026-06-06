package com.portfolio.silver_lady_s.dto.contactinfo;

import com.portfolio.silver_lady_s.entity.ContactInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContactInfoResponse {

    private String phone;
    private String phone2;
    private String email;

    private String address;
    private String addressUz;
    private String addressRu;
    private String addressEn;

    private String workingHours;
    private String workingHoursUz;
    private String workingHoursRu;
    private String workingHoursEn;

    private String locationLink;

    private String instagram;
    private String telegram;
    private String whatsapp;
    private String facebook;
    private String youtube;

    public static ContactInfoResponse from(ContactInfo c) {
        return new ContactInfoResponse(
                c.getPhone(), c.getPhone2(), c.getEmail(),
                c.getAddress(), c.getAddressUz(), c.getAddressRu(), c.getAddressEn(),
                c.getWorkingHours(), c.getWorkingHoursUz(), c.getWorkingHoursRu(), c.getWorkingHoursEn(),
                c.getLocationLink(),
                c.getInstagram(), c.getTelegram(), c.getWhatsapp(), c.getFacebook(), c.getYoutube()
        );
    }
}
