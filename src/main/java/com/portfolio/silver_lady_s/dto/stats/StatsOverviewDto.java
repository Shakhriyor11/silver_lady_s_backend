package com.portfolio.silver_lady_s.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StatsOverviewDto {
    private long todayVisitors;
    private long weekVisitors;
    private long monthVisitors;

    private long todayOrders;
    private long weekOrders;
    private long monthOrders;

    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;

    private long todayNewUsers;
    private long weekNewUsers;
    private long monthNewUsers;

    private long totalUsers;
    private long totalOrders;
    private BigDecimal totalRevenue;
}
