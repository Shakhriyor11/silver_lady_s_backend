package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.PageResponse;
import com.portfolio.silver_lady_s.dto.pos.BarcodeLookupResponse;
import com.portfolio.silver_lady_s.dto.pos.CreateSaleRequest;
import com.portfolio.silver_lady_s.dto.pos.SaleDto;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface SaleService {

    BarcodeLookupResponse lookupByBarcode(String barcode);

    SaleDto recordSale(Long cashierId, CreateSaleRequest request);

    PageResponse<SaleDto> getSales(Long cashierId, Instant from, Instant to, Pageable pageable);

    SaleDto getSaleById(Long id);
}
