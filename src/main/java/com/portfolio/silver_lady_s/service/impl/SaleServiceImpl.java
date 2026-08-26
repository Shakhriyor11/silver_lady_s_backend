package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.PageResponse;
import com.portfolio.silver_lady_s.dto.pos.BarcodeLookupResponse;
import com.portfolio.silver_lady_s.dto.pos.CreateSaleRequest;
import com.portfolio.silver_lady_s.dto.pos.SaleDto;
import com.portfolio.silver_lady_s.dto.pos.SaleItemRequest;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.entity.ProductSizeEntry;
import com.portfolio.silver_lady_s.entity.Sale;
import com.portfolio.silver_lady_s.entity.SaleItem;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.exception.BadRequestException;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.repository.SaleRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.service.SaleService;
import com.portfolio.silver_lady_s.util.PriceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public BarcodeLookupResponse lookupByBarcode(String barcode) {
        Product p = productRepository.findByBarcodeAndActiveTrue(barcode.trim())
                .orElseThrow(() -> new NotFoundException("Product not found for barcode: " + barcode));
        return BarcodeLookupResponse.from(p);
    }

    @Override
    @Transactional
    public SaleDto recordSale(Long cashierId, CreateSaleRequest request) {
        User cashier = userRepository.findById(cashierId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + cashierId));

        List<String> barcodes = request.getItems().stream()
                .map(i -> i.barcode().trim())
                .distinct()
                .toList();

        Map<String, Product> lockedProducts = productRepository.findAllByBarcodesForUpdate(barcodes)
                .stream()
                .collect(Collectors.toMap(Product::getBarcode, p -> p));

        Sale sale = new Sale();
        sale.setCashier(cashier);

        BigDecimal total = BigDecimal.ZERO;
        for (SaleItemRequest itemReq : request.getItems()) {
            String barcode = itemReq.barcode().trim();
            Product product = lockedProducts.get(barcode);
            if (product == null) {
                throw new NotFoundException("Product not found for barcode: " + barcode);
            }
            if (!product.isActive()) {
                throw new BadRequestException("Product is no longer available: " + product.getName());
            }

            ProductSizeEntry entry = product.getSizeEntries().stream()
                    .filter(e -> e.getSize().equals(itemReq.size()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(
                            "'" + itemReq.size() + "' o'lcham " + product.getName() + " uchun mavjud emas."));

            if (entry.getQuantity() < itemReq.quantity()) {
                throw new BadRequestException(
                        "'" + product.getName() + "' (" + itemReq.size() + ") uchun yetarli miqdor yo'q. " +
                        "Mavjud: " + entry.getQuantity() + " dona, talab: " + itemReq.quantity() + " dona.");
            }

            BigDecimal unitPrice = PriceCalculator.computeSalePrice(product);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setProductName(product.getName());
            saleItem.setSize(itemReq.size());
            saleItem.setUnitPrice(unitPrice);
            saleItem.setQuantity(itemReq.quantity());
            saleItem.setLineTotal(lineTotal);
            sale.getItems().add(saleItem);

            total = total.add(lineTotal);

            product.setStockQuantity(product.getStockQuantity() - itemReq.quantity());
            entry.setQuantity(entry.getQuantity() - itemReq.quantity());
        }
        sale.setTotalAmount(total);

        return SaleDto.from(saleRepository.save(sale));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SaleDto> getSales(Long cashierId, Instant from, Instant to, Pageable pageable) {
        Page<Long> idPage = saleRepository.findFilteredIds(cashierId, from, to, pageable);
        List<Long> ids = idPage.getContent();
        if (ids.isEmpty()) {
            return new PageResponse<>(new PageImpl<>(List.of(), pageable, 0));
        }
        Map<Long, Sale> byId = saleRepository.findAllWithItemsByIds(ids).stream()
                .collect(Collectors.toMap(Sale::getId, s -> s));
        List<SaleDto> dtos = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(SaleDto::from)
                .toList();
        return new PageResponse<>(new PageImpl<>(dtos, pageable, idPage.getTotalElements()));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleDto getSaleById(Long id) {
        Sale sale = saleRepository.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Sale not found: id=" + id));
        return SaleDto.from(sale);
    }
}
