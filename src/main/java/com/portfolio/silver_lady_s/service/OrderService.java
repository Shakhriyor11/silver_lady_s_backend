package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.order.CheckoutRequest;
import com.portfolio.silver_lady_s.dto.order.OrderDto;
import com.portfolio.silver_lady_s.dto.order.UpdateOrderStatusRequest;
import com.portfolio.silver_lady_s.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderDto checkout(Long userId, CheckoutRequest request);

    PageResponse<OrderDto> getMyOrders(Long userId, Pageable pageable);

    OrderDto getMyOrder(Long userId, Long orderId);

    OrderDto cancelOrder(Long userId, Long orderId);

    // Admin
    PageResponse<OrderDto> getAllOrders(Pageable pageable);

    OrderDto getOrderById(Long orderId);

    OrderDto updateStatus(Long orderId, UpdateOrderStatusRequest request);
}
