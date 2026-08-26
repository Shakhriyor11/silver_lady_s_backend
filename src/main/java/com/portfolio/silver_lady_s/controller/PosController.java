package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.PageResponse;
import com.portfolio.silver_lady_s.dto.pos.BarcodeLookupResponse;
import com.portfolio.silver_lady_s.dto.pos.CreateSaleRequest;
import com.portfolio.silver_lady_s.dto.pos.SaleDto;
import com.portfolio.silver_lady_s.security.CurrentUser;
import com.portfolio.silver_lady_s.service.SaleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
@Validated
public class PosController {

    private final SaleService saleService;

    @GetMapping("/products/lookup")
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN')")
    public BarcodeLookupResponse lookup(@RequestParam @NotBlank String barcode) {
        return saleService.lookupByBarcode(barcode);
    }

    @PostMapping("/sales")
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN')")
    public ResponseEntity<SaleDto> createSale(@Valid @RequestBody CreateSaleRequest req) {
        Long cashierId = CurrentUser.principal().getUserId();
        SaleDto sale = saleService.recordSale(cashierId, req);
        return ResponseEntity.created(URI.create("/api/pos/sales/" + sale.id())).body(sale);
    }

    @GetMapping("/sales")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<SaleDto> getSales(
            @RequestParam(required = false) Long cashierId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0")  @Min(0)          int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return saleService.getSales(cashierId, from, to, PageRequest.of(page, size));
    }

    @GetMapping("/sales/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SaleDto getSale(@PathVariable Long id) {
        return saleService.getSaleById(id);
    }
}
