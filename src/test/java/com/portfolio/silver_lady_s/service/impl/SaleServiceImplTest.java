package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.pos.BarcodeLookupResponse;
import com.portfolio.silver_lady_s.dto.pos.CreateSaleRequest;
import com.portfolio.silver_lady_s.dto.pos.SaleDto;
import com.portfolio.silver_lady_s.dto.pos.SaleItemRequest;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.entity.ProductSizeEntry;
import com.portfolio.silver_lady_s.entity.Sale;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.exception.BadRequestException;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.repository.SaleRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock private SaleRepository saleRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private SaleServiceImpl saleService;

    private User cashier;
    private Product product;
    private ProductSizeEntry sizeEntry;

    @BeforeEach
    void setUp() {
        cashier = new User();
        cashier.setId(1L);
        cashier.setFullName("Kassir Malika");

        product = new Product();
        product.setId(10L);
        product.setName("Kumush uzuk");
        product.setBarcode("4780123456789");
        product.setPrice(new BigDecimal("150000.00"));
        product.setStockQuantity(5);
        product.setActive(true);

        sizeEntry = new ProductSizeEntry();
        sizeEntry.setProduct(product);
        sizeEntry.setSize("16");
        sizeEntry.setQuantity(5);
        product.getSizeEntries().add(sizeEntry);
    }

    // ── lookupByBarcode ──────────────────────────────────────────────────────────

    @Test
    void lookupByBarcode_found_returnsSizeEntries() {
        when(productRepository.findByBarcodeAndActiveTrue("4780123456789")).thenReturn(Optional.of(product));

        BarcodeLookupResponse result = saleService.lookupByBarcode("4780123456789");

        assertThat(result.productId()).isEqualTo(10L);
        assertThat(result.sizeEntries()).hasSize(1);
        assertThat(result.sizeEntries().get(0).size()).isEqualTo("16");
        assertThat(result.sizeEntries().get(0).quantity()).isEqualTo(5);
    }

    @Test
    void lookupByBarcode_notFound_throwsNotFoundException() {
        when(productRepository.findByBarcodeAndActiveTrue("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.lookupByBarcode("unknown"))
                .isInstanceOf(NotFoundException.class);
    }

    // ── recordSale ───────────────────────────────────────────────────────────────

    @Test
    void recordSale_success_decrementsSizeEntryAndStockQuantity() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(cashier));
        when(productRepository.findAllByBarcodesForUpdate(List.of("4780123456789"))).thenReturn(List.of(product));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 2)));

        SaleDto result = saleService.recordSale(1L, req);

        assertThat(result.totalAmount()).isEqualByComparingTo("300000.00");
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).quantity()).isEqualTo(2);
        assertThat(sizeEntry.getQuantity()).isEqualTo(3);
        assertThat(product.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void recordSale_appliesActiveDiscount_toUnitPrice() {
        product.setDiscountPercent(10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(cashier));
        when(productRepository.findAllByBarcodesForUpdate(List.of("4780123456789"))).thenReturn(List.of(product));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 1)));

        SaleDto result = saleService.recordSale(1L, req);

        assertThat(result.items().get(0).unitPrice()).isEqualByComparingTo("135000.00");
    }

    @Test
    void recordSale_insufficientStock_throwsBadRequestException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(cashier));
        when(productRepository.findAllByBarcodesForUpdate(List.of("4780123456789"))).thenReturn(List.of(product));

        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 10)));

        assertThatThrownBy(() -> saleService.recordSale(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("yetarli miqdor");
    }

    @Test
    void recordSale_unknownBarcode_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(cashier));
        when(productRepository.findAllByBarcodesForUpdate(List.of("unknown"))).thenReturn(List.of());

        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("unknown", "16", 1)));

        assertThatThrownBy(() -> saleService.recordSale(1L, req))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void recordSale_unknownSize_throwsBadRequestException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(cashier));
        when(productRepository.findAllByBarcodesForUpdate(List.of("4780123456789"))).thenReturn(List.of(product));

        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "99", 1)));

        assertThatThrownBy(() -> saleService.recordSale(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("mavjud emas");
    }

    @Test
    void recordSale_inactiveProduct_throwsBadRequestException() {
        product.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(cashier));
        when(productRepository.findAllByBarcodesForUpdate(List.of("4780123456789"))).thenReturn(List.of(product));

        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 1)));

        assertThatThrownBy(() -> saleService.recordSale(1L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no longer available");
    }

    @Test
    void recordSale_cashierNotFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 1)));

        assertThatThrownBy(() -> saleService.recordSale(99L, req))
                .isInstanceOf(NotFoundException.class);
    }
}
