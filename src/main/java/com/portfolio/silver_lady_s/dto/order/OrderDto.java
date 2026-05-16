package com.portfolio.silver_lady_s.dto.order;

import com.portfolio.silver_lady_s.entity.Order;
import com.portfolio.silver_lady_s.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDto(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        String shippingAddress,
        String phone,
        String userFullName,
        String userEmail,
        List<OrderItemDto> items,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderDto from(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(OrderItemDto::from)
                .toList();
        return new OrderDto(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getPhone(),
                order.getUser() != null ? order.getUser().getFullName() : null,
                order.getUser() != null ? order.getUser().getEmail()    : null,
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
