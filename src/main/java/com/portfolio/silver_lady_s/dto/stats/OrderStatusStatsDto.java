package com.portfolio.silver_lady_s.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderStatusStatsDto {
    private List<String> statuses;
    private List<Long>   counts;
}
