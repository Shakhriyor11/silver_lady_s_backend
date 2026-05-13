package com.portfolio.silver_lady_s.entity;

public enum OrderStatus {
    PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED;

    /**
     * Admin tomonidan ruxsat etilgan holat o'tishlari:
     *   PENDING   → CONFIRMED | CANCELLED
     *   CONFIRMED → SHIPPED   | CANCELLED
     *   SHIPPED   → DELIVERED
     *   DELIVERED, CANCELLED → yakuniy holat, o'tish yo'q
     */
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING    -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED  -> next == SHIPPED   || next == CANCELLED;
            case SHIPPED    -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
