package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.PageResponse;
import com.portfolio.silver_lady_s.dto.order.CheckoutRequest;
import com.portfolio.silver_lady_s.dto.order.OrderDto;
import com.portfolio.silver_lady_s.dto.order.UpdateOrderStatusRequest;
import com.portfolio.silver_lady_s.entity.*;
import com.portfolio.silver_lady_s.exception.BadRequestException;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.*;
import com.portfolio.silver_lady_s.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderDto checkout(Long userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        for (CartItem ci : cartItems) {
            if (!ci.getProduct().isActive()) {
                throw new BadRequestException(
                        "Product is no longer available: " + ci.getProduct().getName());
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress().trim());
        order.setPhone(resolvePhone(request.getPhone(), user.getPhone()));
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            BigDecimal lineTotal = ci.getUnitPrice()
                    .multiply(BigDecimal.valueOf(ci.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(ci.getProduct());
            item.setProductName(ci.getProduct().getName());
            item.setUnitPrice(ci.getUnitPrice());
            item.setQuantity(ci.getQuantity());
            item.setLineTotal(lineTotal);

            order.getItems().add(item);
            total = total.add(lineTotal);
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        cartItemRepository.deleteByCartId(cart.getId());

        return OrderDto.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDto> getMyOrders(Long userId, Pageable pageable) {
        Page<OrderDto> page = orderRepository.findByUserId(userId, pageable)
                .map(OrderDto::from);
        return new PageResponse<>(page);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getMyOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found: id=" + orderId));
        return OrderDto.from(order);
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found: id=" + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING orders can be cancelled. Current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return OrderDto.from(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDto> getAllOrders(Pageable pageable) {
        Page<Long> idPage = orderRepository.findAllIds(pageable);
        List<Long> ids = idPage.getContent();
        if (ids.isEmpty()) {
            return new PageResponse<>(new PageImpl<>(List.of(), pageable, 0));
        }
        List<Order> orders = orderRepository.findAllWithItemsByIds(ids);
        Map<Long, Order> byId = orders.stream()
                .collect(Collectors.toMap(Order::getId, o -> o));
        List<OrderDto> dtos = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(OrderDto::from)
                .toList();
        return new PageResponse<>(new PageImpl<>(dtos, pageable, idPage.getTotalElements()));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: id=" + orderId));
        return OrderDto.from(order);
    }

    @Override
    @Transactional
    public OrderDto updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: id=" + orderId));

        OrderStatus current = order.getStatus();
        OrderStatus next    = request.getStatus();

        if (!current.canTransitionTo(next)) {
            throw new BadRequestException(
                    "Invalid status transition: " + current + " → " + next);
        }

        order.setStatus(next);
        return OrderDto.from(orderRepository.save(order));
    }

    private String resolvePhone(String requestPhone, String userPhone) {
        if (requestPhone != null && !requestPhone.isBlank()) {
            return requestPhone.trim();
        }
        return userPhone;
    }
}
