package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.stats.DailyStatsDto;
import com.portfolio.silver_lady_s.dto.stats.OrderStatusStatsDto;
import com.portfolio.silver_lady_s.dto.stats.StatsOverviewDto;
import com.portfolio.silver_lady_s.dto.stats.TopProductDto;

import java.util.List;

public interface StatsService {

    void recordVisit(String visitorId, String ipHash);

    StatsOverviewDto getOverview();

    DailyStatsDto getDaily(int days);

    OrderStatusStatsDto getOrderStatusStats();

    List<TopProductDto> getTopViewedProducts(int limit);
}
