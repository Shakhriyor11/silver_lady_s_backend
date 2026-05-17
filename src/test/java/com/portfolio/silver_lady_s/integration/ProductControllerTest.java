package com.portfolio.silver_lady_s.integration;

import com.portfolio.silver_lady_s.dto.product.CreateProductRequest;
import com.portfolio.silver_lady_s.dto.product.UpdateProductRequest;
import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.repository.CategoryRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest extends AbstractIntegrationTest {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Uzuklar");
        category = categoryRepository.save(category);
    }

    // ── GET /api/products ─────────────────────────────────────────────────────────

    @Test
    void getProducts_empty_returns200WithEmptyPage() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getProducts_withMultipleProducts_returnsPaginated() throws Exception {
        saveProduct("Oltin uzuk", "500000.00");
        saveProduct("Kumush uzuk", "200000.00");

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getProducts_withSearch_returnsMatchingOnly() throws Exception {
        saveProduct("Oltin uzuk", "500000.00");
        saveProduct("Kumush bilaguzuk", "200000.00");

        mockMvc.perform(get("/api/products").param("search", "Oltin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Oltin uzuk"));
    }

    @Test
    void getProducts_withCategoryFilter_returnsOnlyThatCategory() throws Exception {
        Category other = categoryRepository.save(newCategory("Bilaguzuklar"));

        saveProduct("Uzuk 1", "100000.00");
        Product p2 = new Product();
        p2.setName("Bilaguzuk 1");
        p2.setPrice(new BigDecimal("150000.00"));
        p2.getCategories().add(other);
        productRepository.save(p2);

        mockMvc.perform(get("/api/products").param("categoryId", category.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].categories[0].id").value(category.getId()));
    }

    @Test
    void getProducts_inactiveProductsExcluded() throws Exception {
        saveProduct("Ko'rinadigan uzuk", "100000.00");
        Product inactive = saveProduct("Ko'rinmaydigan uzuk", "100000.00");
        inactive.setActive(false);
        productRepository.save(inactive);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ── GET /api/products/{id} ────────────────────────────────────────────────────

    @Test
    void getProduct_existingId_returns200WithDetails() throws Exception {
        Product p = saveProduct("Oltin uzuk", "500000.00");

        mockMvc.perform(get("/api/products/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(p.getId()))
                .andExpect(jsonPath("$.name").value("Oltin uzuk"))
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getProduct_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProduct_inactiveProduct_returns404() throws Exception {
        Product p = saveProduct("Arxivdagi uzuk", "100000.00");
        p.setActive(false);
        productRepository.save(p);

        mockMvc.perform(get("/api/products/" + p.getId()))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/products/{id}/similar ────────────────────────────────────────────

    @Test
    void getSimilarProducts_returnsProductsFromSameCategory() throws Exception {
        Product base = saveProduct("Oltin uzuk", "500000.00");
        saveProduct("Oltin sirg'a", "300000.00");
        saveProduct("Oltin marjon", "400000.00");

        mockMvc.perform(get("/api/products/" + base.getId() + "/similar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ── POST /api/products ────────────────────────────────────────────────────────

    @Test
    void createProduct_admin_returns201WithLocationHeader() throws Exception {
        String token = adminToken();

        CreateProductRequest req = buildCreateRequest("Yangi uzuk", "300000.00");

        String body = mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Yangi uzuk"))
                .andExpect(jsonPath("$.price").isNumber())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(body).get("id").asLong();
        assertThat(id).isPositive();
    }

    @Test
    void createProduct_admin_nameIsTrimed() throws Exception {
        String token = adminToken();
        CreateProductRequest req = buildCreateRequest("  Uzuk  ", "100000.00");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Uzuk"));
    }

    @Test
    void createProduct_noAuth_returns401() throws Exception {
        CreateProductRequest req = buildCreateRequest("Uzuk", "100000.00");

        mockMvc.perform(post("/api/products")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProduct_userRole_returns403() throws Exception {
        String token = userToken();
        CreateProductRequest req = buildCreateRequest("Uzuk", "100000.00");

        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_invalidCategory_returns404() throws Exception {
        String token = adminToken();

        CreateProductRequest req = new CreateProductRequest();
        req.setName("Uzuk");
        req.setPrice(new BigDecimal("100000.00"));
        req.setCategoryIds(List.of(99999L));

        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_missingPrice_returns400() throws Exception {
        String token = adminToken();

        CreateProductRequest req = new CreateProductRequest();
        req.setName("Uzuk");
        req.setCategoryIds(List.of(category.getId()));
        // price yo'q

        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/products/{id} ────────────────────────────────────────────────────

    @Test
    void updateProduct_admin_returns200WithUpdatedFields() throws Exception {
        String token = adminToken();
        Product p = saveProduct("Eski uzuk", "100000.00");

        UpdateProductRequest req = new UpdateProductRequest();
        req.setName("Yangilangan uzuk");
        req.setDescription("Yangi tavsif");
        req.setPrice(new BigDecimal("150000.00"));
        req.setCategoryIds(List.of(category.getId()));

        mockMvc.perform(put("/api/products/" + p.getId())
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yangilangan uzuk"))
                .andExpect(jsonPath("$.description").value("Yangi tavsif"));
    }

    @Test
    void updateProduct_notFound_returns404() throws Exception {
        String token = adminToken();

        UpdateProductRequest req = new UpdateProductRequest();
        req.setName("Uzuk");
        req.setPrice(new BigDecimal("100000.00"));
        req.setCategoryIds(List.of(category.getId()));

        mockMvc.perform(put("/api/products/999999")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/products/{id} (soft delete) ───────────────────────────────────

    @Test
    void deleteProduct_admin_softDeletesAndReturns204() throws Exception {
        String token = adminToken();
        Product p = saveProduct("O'chiriladigan uzuk", "100000.00");

        mockMvc.perform(delete("/api/products/" + p.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/" + p.getId()))
                .andExpect(status().isNotFound());

        Product inDb = productRepository.findById(p.getId()).orElseThrow();
        assertThat(inDb.isActive()).isFalse();
    }

    @Test
    void deleteProduct_noAuth_returns401() throws Exception {
        Product p = saveProduct("Uzuk", "100000.00");

        mockMvc.perform(delete("/api/products/" + p.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteProduct_userRole_returns403() throws Exception {
        String token = userToken();
        Product p = saveProduct("Uzuk", "100000.00");

        mockMvc.perform(delete("/api/products/" + p.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    // ── PATCH /api/products/{id}/restore ──────────────────────────────────────────

    @Test
    void restoreProduct_admin_setsActiveTrueAndReturns200() throws Exception {
        String token = adminToken();
        Product p = saveProduct("Arxivdagi uzuk", "100000.00");
        p.setActive(false);
        productRepository.save(p);

        mockMvc.perform(patch("/api/products/" + p.getId() + "/restore")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/products/" + p.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void restoreProduct_alreadyActive_returns404() throws Exception {
        String token = adminToken();
        Product p = saveProduct("Faol uzuk", "100000.00");

        mockMvc.perform(patch("/api/products/" + p.getId() + "/restore")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    // ── helpers ───────────────────────────────────────────────────────────────────

    private Product saveProduct(String name, String price) {
        Product p = new Product();
        p.setName(name);
        p.setDescription("Test tavsif");
        p.setPrice(new BigDecimal(price));
        p.getCategories().add(category);
        return productRepository.save(p);
    }

    private Category newCategory(String name) {
        Category c = new Category();
        c.setName(name);
        return c;
    }

    private CreateProductRequest buildCreateRequest(String name, String price) {
        CreateProductRequest req = new CreateProductRequest();
        req.setName(name);
        req.setDescription("Test tavsif");
        req.setPrice(new BigDecimal(price));
        req.setCategoryIds(List.of(category.getId()));
        return req;
    }
}
