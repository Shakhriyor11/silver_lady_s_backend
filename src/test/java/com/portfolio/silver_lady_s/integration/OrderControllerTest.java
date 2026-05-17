package com.portfolio.silver_lady_s.integration;

import com.portfolio.silver_lady_s.dto.cart.AddToCartRequest;
import com.portfolio.silver_lady_s.dto.order.CheckoutRequest;
import com.portfolio.silver_lady_s.dto.order.UpdateOrderStatusRequest;
import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.entity.OrderStatus;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.repository.CategoryRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest extends AbstractIntegrationTest {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        Category c = new Category();
        c.setName("Uzuklar");
        c = categoryRepository.save(c);

        product = new Product();
        product.setName("Oltin uzuk");
        product.setDescription("Test mahsulot");
        product.setPrice(new BigDecimal("500000.00"));
        product.getCategories().add(c);
        product = productRepository.save(product);
    }

    // ── POST /api/orders (checkout) ───────────────────────────────────────────────

    @Test
    void checkout_noAuth_returns401() throws Exception {
        CheckoutRequest req = buildCheckoutRequest("Toshkent, Yunusobod");

        mockMvc.perform(post("/api/orders")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkout_emptyCart_returns400() throws Exception {
        String token = userToken();

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(buildCheckoutRequest("Toshkent"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_missingShippingAddress_returns400() throws Exception {
        String token = userToken();
        addToCart(token, product.getId(), 1);

        CheckoutRequest req = new CheckoutRequest();
        // shippingAddress yo'q

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_withCartItems_returns201AndClearsCart() throws Exception {
        String token = userToken();
        addToCart(token, product.getId(), 2);

        String body = mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(buildCheckoutRequest("Toshkent, Yunusobod 14"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.shippingAddress").value("Toshkent, Yunusobod 14"))
                .andReturn().getResponse().getContentAsString();

        // totalAmount = 500000 * 2
        BigDecimal total = new BigDecimal(objectMapper.readTree(body).get("totalAmount").asText());
        org.assertj.core.api.Assertions.assertThat(total).isEqualByComparingTo("1000000.00");

        // Cart tozalanganligini tekshiramiz
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void checkout_inactiveProduct_returns400() throws Exception {
        String token = userToken();
        addToCart(token, product.getId(), 1);

        // Mahsulotni o'chiramiz (soft delete)
        product.setActive(false);
        productRepository.save(product);

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(buildCheckoutRequest("Toshkent"))))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/orders/my ────────────────────────────────────────────────────────

    @Test
    void getMyOrders_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/orders/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyOrders_returnsOnlyUserOrders() throws Exception {
        String token = userToken();
        placeOrder(token);

        mockMvc.perform(get("/api/orders/my")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getMyOrders_isolatedBetweenUsers() throws Exception {
        String token1 = userToken();
        placeOrder(token1);

        // user2 o'z buyurtmalarini ko'rsa — bo'sh bo'lishi kerak
        register("user2@test.com", "User1234!");
        String token2 = loginToken("user2@test.com", "User1234!");

        mockMvc.perform(get("/api/orders/my")
                        .header("Authorization", bearer(token2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ── GET /api/orders/my/{id} ───────────────────────────────────────────────────

    @Test
    void getMyOrder_existingOrder_returns200() throws Exception {
        String token = userToken();
        Long orderId = placeOrder(token);

        mockMvc.perform(get("/api/orders/my/" + orderId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    void getMyOrder_otherUserOrder_returns404() throws Exception {
        String token1 = userToken();
        Long orderId = placeOrder(token1);

        register("user2@test.com", "User1234!");
        String token2 = loginToken("user2@test.com", "User1234!");

        mockMvc.perform(get("/api/orders/my/" + orderId)
                        .header("Authorization", bearer(token2)))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/orders/my/{id}/cancel ──────────────────────────────────────────

    @Test
    void cancelOrder_pendingOrder_returns200WithCancelledDto() throws Exception {
        String token = userToken();
        Long orderId = placeOrder(token);

        mockMvc.perform(patch("/api/orders/my/" + orderId + "/cancel")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelOrder_otherUserOrder_returns404() throws Exception {
        String token1 = userToken();
        Long orderId = placeOrder(token1);

        register("user2@test.com", "User1234!");
        String token2 = loginToken("user2@test.com", "User1234!");

        mockMvc.perform(patch("/api/orders/my/" + orderId + "/cancel")
                        .header("Authorization", bearer(token2)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelOrder_noAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/orders/my/1/cancel"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/orders (admin) ───────────────────────────────────────────────────

    @Test
    void getAllOrders_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllOrders_userRole_returns403() throws Exception {
        String token = userToken();

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllOrders_admin_returnsAllOrders() throws Exception {
        String userToken = userToken();
        placeOrder(userToken);

        String adminToken = adminToken();

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ── PATCH /api/orders/{id}/status (admin) ─────────────────────────────────────

    @Test
    void updateStatus_admin_pendingToConfirmed() throws Exception {
        String userToken = userToken();
        Long orderId = placeOrder(userToken);
        String adminToken = adminToken();

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.CONFIRMED);

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .header("Authorization", bearer(adminToken))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void updateStatus_admin_fullFlow() throws Exception {
        String userToken = userToken();
        Long orderId = placeOrder(userToken);
        String adminToken = adminToken();

        // PENDING → CONFIRMED
        patchStatus(adminToken, orderId, OrderStatus.CONFIRMED).andExpect(status().isOk());
        // CONFIRMED → SHIPPED
        patchStatus(adminToken, orderId, OrderStatus.SHIPPED).andExpect(status().isOk());
        // SHIPPED → DELIVERED
        patchStatus(adminToken, orderId, OrderStatus.DELIVERED)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void updateStatus_admin_invalidTransition_returns400() throws Exception {
        String userToken = userToken();
        Long orderId = placeOrder(userToken);
        String adminToken = adminToken();

        // PENDING → SHIPPED is invalid (must go through CONFIRMED first)
        patchStatus(adminToken, orderId, OrderStatus.SHIPPED)
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_userRole_returns403() throws Exception {
        String userToken = userToken();
        Long orderId = placeOrder(userToken);

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.CONFIRMED);

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .header("Authorization", bearer(userToken))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_noAuth_returns401() throws Exception {
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.SHIPPED);

        mockMvc.perform(patch("/api/orders/1/status")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    // ── helpers ───────────────────────────────────────────────────────────────────

    private void addToCart(String token, Long productId, int quantity) throws Exception {
        AddToCartRequest req = new AddToCartRequest();
        req.setProductId(productId);
        req.setQuantity(quantity);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk());
    }

    private Long placeOrder(String token) throws Exception {
        addToCart(token, product.getId(), 1);

        String body = mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(buildCheckoutRequest("Toshkent, Test ko'chasi 1"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    private CheckoutRequest buildCheckoutRequest(String address) {
        CheckoutRequest req = new CheckoutRequest();
        req.setShippingAddress(address);
        return req;
    }

    private org.springframework.test.web.servlet.ResultActions patchStatus(
            String token, Long orderId, OrderStatus status) throws Exception {
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(status);
        return mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                .header("Authorization", bearer(token))
                .contentType(APPLICATION_JSON)
                .content(toJson(req)));
    }
}
