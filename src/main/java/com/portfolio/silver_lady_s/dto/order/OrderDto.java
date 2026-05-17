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
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
