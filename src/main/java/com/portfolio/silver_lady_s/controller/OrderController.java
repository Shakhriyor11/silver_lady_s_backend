package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.PageResponse;
import com.portfolio.silver_lady_s.dto.order.CheckoutRequest;
import com.portfolio.silver_lady_s.dto.order.OrderDto;
import com.portfolio.silver_lady_s.dto.order.UpdateOrderStatusRequest;
import com.portfolio.silver_lady_s.security.CurrentUser;
import com.portfolio.silver_lady_s.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── User endpoints ──────────────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto checkout(@Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(CurrentUser.principal().getUserId(), request);
    }

    @GetMapping("/my")
    public PageResponse<OrderDto> getMyOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.getMyOrders(
                CurrentUser.principal().getUserId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @GetMapping("/my/{id}")
    public OrderDto getMyOrder(@PathVariable Long id) {
        return orderService.getMyOrder(CurrentUser.principal().getUserId(), id);
    }

    @PatchMapping("/my/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(CurrentUser.principal().getUserId(), id);
    }

    // ── Admin endpoints ─────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<OrderDto> getAllOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String dir) {
        Sort sort = dir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return orderService.getAllOrders(PageRequest.of(page, size, sort));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDto getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDto updateStatus(@PathVariable Long id,
                                 @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request);
    }
}
