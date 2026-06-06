package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.contactinfo.ContactInfoResponse;
import com.portfolio.silver_lady_s.dto.contactinfo.ContactInfoUpdateRequest;

public interface ContactInfoService {
    ContactInfoResponse get();
    ContactInfoResponse update(ContactInfoUpdateRequest dto);
}
