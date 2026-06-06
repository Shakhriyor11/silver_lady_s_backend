package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.config.CacheConfig;
import com.portfolio.silver_lady_s.dto.contactinfo.ContactInfoResponse;
import com.portfolio.silver_lady_s.dto.contactinfo.ContactInfoUpdateRequest;
import com.portfolio.silver_lady_s.entity.ContactInfo;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.ContactInfoRepository;
import com.portfolio.silver_lady_s.service.ContactInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactInfoServiceImpl implements ContactInfoService {

    private final ContactInfoRepository contactInfoRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.CACHE_CONTACT_INFO)
    public ContactInfoResponse get() {
        ContactInfo c = contactInfoRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException("Contact info not found"));
        return ContactInfoResponse.from(c);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CONTACT_INFO, allEntries = true)
    public ContactInfoResponse update(ContactInfoUpdateRequest dto) {
        ContactInfo c = contactInfoRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new NotFoundException("Contact info not found"));

        c.setPhone(dto.getPhone().trim());
        c.setPhone2(dto.getPhone2() == null ? null : dto.getPhone2().trim());
        c.setEmail(dto.getEmail() == null ? null : dto.getEmail().trim());

        c.setAddress(dto.getAddress().trim());
        c.setAddressUz(dto.getAddressUz());
        c.setAddressRu(dto.getAddressRu());
        c.setAddressEn(dto.getAddressEn());

        c.setWorkingHours(dto.getWorkingHours().trim());
        c.setWorkingHoursUz(dto.getWorkingHoursUz());
        c.setWorkingHoursRu(dto.getWorkingHoursRu());
        c.setWorkingHoursEn(dto.getWorkingHoursEn());

        c.setLocationLink(dto.getLocationLink() == null ? null : dto.getLocationLink().trim());

        c.setInstagram(dto.getInstagram() == null ? null : dto.getInstagram().trim());
        c.setTelegram(dto.getTelegram() == null ? null : dto.getTelegram().trim());
        c.setWhatsapp(dto.getWhatsapp() == null ? null : dto.getWhatsapp().trim());
        c.setFacebook(dto.getFacebook() == null ? null : dto.getFacebook().trim());
        c.setYoutube(dto.getYoutube() == null ? null : dto.getYoutube().trim());

        return ContactInfoResponse.from(contactInfoRepository.save(c));
    }
}
