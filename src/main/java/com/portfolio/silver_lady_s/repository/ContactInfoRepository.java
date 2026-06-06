package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {
    Optional<ContactInfo> findTopByOrderByIdAsc();
}
