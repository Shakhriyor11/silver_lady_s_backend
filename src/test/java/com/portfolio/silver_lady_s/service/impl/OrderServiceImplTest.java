package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.order.CheckoutRequest;
import com.portfolio.silver_lady_s.dto.order.OrderDto;
import com.portfolio.silver_lady_s.dto.order.UpdateOrderStatusRequest;
import com.portfolio.silver_lady_s.entity.*;
import com.portfolio.silver_lady_s.exception.BadRequestException;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private OrderServiceImpl orderService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Uzuklar");

        product = new Product();
        product.setId(10L);
        product.setName("Oltin uzuk");
        product.setPrice(new BigDecimal("150000.00"));
        product.setCategory(category);
        product.setActive(true);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPhone("+998901234567");

        cart = new Cart();
        cart.setId(5L);
        cart.setUser(user);
    }

    // ── checkout ─────────────────────────────────────────────────────────────────

    @Test
    void checkout_success_createsOrderAndClearsCart() {
        CartItem cartItem = makeCartItem(product, 2, new BigDecimal("150000.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(5L)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckoutRequest request = new CheckoutRequest();
        request.setShippingAddress("Toshkent, Chilonzor");

        OrderDto result = orderService.checkout(1L, request);

        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.totalAmount()).isEqualByComparingTo("300000.00");
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).productName()).isEqualTo("Oltin uzuk");
        assertThat(result.items().get(0).quantity()).isEqualTo(2);
        assertThat(result.items().get(0).lineTotal()).isEqualByComparingTo("300000.00");
        assertThat(result.phone()).isEqualTo("+998901234567"); // user phone fallback

        verify(cartItemRepository).deleteByCartId(5L);
    }

    @Test
    void checkout_phoneInRequest_usesRequestPhone() {
        CartItem cartItem = makeCartItem(product, 1, new BigDecimal("100000.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(5L)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckoutRequest request = new CheckoutRequest();
        request.setShippingAddress("Samarqand");
        request.setPhone("+998711112233");

        OrderDto result = orderService.checkout(1L, request);

        assertThat(result.phone()).isEqualTo("+998711112233");
    }

    @Test
    void checkout_cartNotFound_throwsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.checkout(1L, new CheckoutRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void checkout_emptyCart_throwsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(5L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.checkout(1L, new CheckoutRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void checkout_inactiveProduct_throwsBadRequest() {
        product.setActive(false);
        CartItem cartItem = makeCartItem(product, 1, new BigDecimal("150000.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(5L)).thenReturn(List.of(cartItem));

        CheckoutRequest request = new CheckoutRequest();
        request.setShippingAddress("Toshkent");

        assertThatThrownBy(() -> orderService.checkout(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no longer available");
    }

    @Test
    void checkout_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.checkout(99L, new CheckoutRequest()))
                .isInstanceOf(NotFoundException.class);
    }

    // ── cancelOrder ──────────────────────────────────────────────────────────────

    @Test
    void cancelOrder_pending_changesStatusToCancelled() {
        Order order = makeOrder(OrderStatus.PENDING);
        when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.cancelOrder(1L, 100L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_confirmedOrder_throwsBadRequest() {
        Order order = makeOrder(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    void cancelOrder_deliveredOrder_throwsBadRequest() {
        Order order = makeOrder(OrderStatus.DELIVERED);
        when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 100L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void cancelOrder_notFound_throwsNotFoundException() {
        when(orderRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    // ── getMyOrder ───────────────────────────────────────────────────────────────

    @Test
    void getMyOrder_found_returnsDto() {
        Order order = makeOrder(OrderStatus.PENDING);
        when(orderRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(order));

        OrderDto result = orderService.getMyOrder(1L, 100L);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.totalAmount()).isEqualByComparingTo("150000.00");
    }

    @Test
    void getMyOrder_notFound_throwsNotFoundException() {
        when(orderRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getMyOrder(1L, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    // ── updateStatus (admin) ─────────────────────────────────────────────────────

    @Test
    void updateStatus_pendingToConfirmed_success() {
        Order order = makeOrder(OrderStatus.PENDING);
        when(orderRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.CONFIRMED);

        OrderDto result = orderService.updateStatus(100L, req);

        assertThat(result.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateStatus_invalidTransition_throwsBadRequest() {
        Order order = makeOrder(OrderStatus.PENDING);
        when(orderRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.SHIPPED); // PENDING → SHIPPED noto'g'ri

        assertThatThrownBy(() -> orderService.updateStatus(100L, req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_terminalState_throwsBadRequest() {
        Order order = makeOrder(OrderStatus.DELIVERED);
        when(orderRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.PENDING); // DELIVERED → PENDING mumkin emas

        assertThatThrownBy(() -> orderService.updateStatus(100L, req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void updateStatus_notFound_throwsNotFoundException() {
        when(orderRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.CONFIRMED);

        assertThatThrownBy(() -> orderService.updateStatus(999L, req))
                .isInstanceOf(NotFoundException.class);
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private CartItem makeCartItem(Product p, int qty, BigDecimal unitPrice) {
        CartItem ci = new CartItem();
        ci.setId(20L);
        ci.setCart(cart);
        ci.setProduct(p);
        ci.setQuantity(qty);
        ci.setUnitPrice(unitPrice);
        return ci;
    }

    private Order makeOrder(OrderStatus status) {
        Order order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("150000.00"));
        order.setShippingAddress("Toshkent");
        return order;
    }
}
