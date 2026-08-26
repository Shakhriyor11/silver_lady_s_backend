package com.portfolio.silver_lady_s.integration;

import com.portfolio.silver_lady_s.dto.pos.CreateSaleRequest;
import com.portfolio.silver_lady_s.dto.pos.SaleItemRequest;
import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.entity.ProductSizeEntry;
import com.portfolio.silver_lady_s.repository.CategoryRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PosControllerTest extends AbstractIntegrationTest {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(newCategory());

        product = new Product();
        product.setName("Kumush uzuk");
        product.setPrice(new BigDecimal("150000.00"));
        product.getCategories().add(category);

        ProductSizeEntry entry = new ProductSizeEntry();
        entry.setProduct(product);
        entry.setSize("16");
        entry.setQuantity(5);
        product.getSizeEntries().add(entry);
        product.setStockQuantity(5);
        product.setBarcode("4780123456789");

        product = productRepository.save(product);
    }

    // ── GET /api/pos/products/lookup ──────────────────────────────────────────────

    @Test
    void lookup_byBarcode_asCashier_returnsProductWithSizes() throws Exception {
        String token = cashierToken();

        mockMvc.perform(get("/api/pos/products/lookup")
                        .header("Authorization", bearer(token))
                        .param("barcode", "4780123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(product.getId()))
                .andExpect(jsonPath("$.sizeEntries[0].size").value("16"))
                .andExpect(jsonPath("$.sizeEntries[0].quantity").value(5));
    }

    @Test
    void lookup_unknownBarcode_returns404() throws Exception {
        String token = cashierToken();

        mockMvc.perform(get("/api/pos/products/lookup")
                        .header("Authorization", bearer(token))
                        .param("barcode", "0000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void lookup_asPlainUser_returns403() throws Exception {
        String token = userToken();

        mockMvc.perform(get("/api/pos/products/lookup")
                        .header("Authorization", bearer(token))
                        .param("barcode", "4780123456789"))
                .andExpect(status().isForbidden());
    }

    // ── POST /api/pos/sales ────────────────────────────────────────────────────────

    @Test
    void createSale_asCashier_success_decrementsInventoryAndReturnsReceipt() throws Exception {
        String token = cashierToken();

        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 2)));

        mockMvc.perform(post("/api/pos/sales")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(300000.00))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        Product afterSale = productRepository.findById(product.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(afterSale.getStockQuantity()).isEqualTo(3);
        org.assertj.core.api.Assertions.assertThat(afterSale.getSizeEntries().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void createSale_insufficientStock_returns400() throws Exception {
        String token = cashierToken();

        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 99)));

        mockMvc.perform(post("/api/pos/sales")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSale_asAdmin_alsoAllowed() throws Exception {
        String token = adminToken();

        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 1)));

        mockMvc.perform(post("/api/pos/sales")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void createSale_asPlainUser_returns403() throws Exception {
        String token = userToken();

        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 1)));

        mockMvc.perform(post("/api/pos/sales")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createSale_unauthenticated_returns401() throws Exception {
        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 1)));

        mockMvc.perform(post("/api/pos/sales")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/pos/sales ────────────────────────────────────────────────────────

    @Test
    void getSales_asCashier_returns403() throws Exception {
        String token = cashierToken();

        mockMvc.perform(get("/api/pos/sales")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSales_asAdmin_returnsSale() throws Exception {
        String cashier = cashierToken();
        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 1)));
        mockMvc.perform(post("/api/pos/sales")
                        .header("Authorization", bearer(cashier))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated());

        String admin = adminToken();
        mockMvc.perform(get("/api/pos/sales")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getSaleById_asAdmin_returnsSaleWithItems() throws Exception {
        String cashier = cashierToken();
        CreateSaleRequest req = new CreateSaleRequest();
        req.setItems(List.of(new SaleItemRequest("4780123456789", "16", 1)));
        String body = mockMvc.perform(post("/api/pos/sales")
                        .header("Authorization", bearer(cashier))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long saleId = objectMapper.readTree(body).get("id").asLong();

        String admin = adminToken();
        mockMvc.perform(get("/api/pos/sales/" + saleId)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].size").value("16"));
    }

    private Category newCategory() {
        Category c = new Category();
        c.setName("Uzuklar");
        return c;
    }
}
