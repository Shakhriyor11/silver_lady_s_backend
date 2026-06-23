package com.portfolio.silver_lady_s.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class DailyStatsDto {
    private List<String>     dates;
    private List<Long>       visitors;
    private List<Long>       orders;
    private List<BigDecimal> revenue;
    private List<Long>       newUsers;
}
