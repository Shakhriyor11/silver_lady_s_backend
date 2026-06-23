package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.stats.DailyStatsDto;
import com.portfolio.silver_lady_s.dto.stats.OrderStatusStatsDto;
import com.portfolio.silver_lady_s.dto.stats.StatsOverviewDto;
import com.portfolio.silver_lady_s.dto.stats.TopProductDto;
import com.portfolio.silver_lady_s.entity.SiteVisit;
import com.portfolio.silver_lady_s.repository.OrderRepository;
import com.portfolio.silver_lady_s.repository.ProductViewRepository;
import com.portfolio.silver_lady_s.repository.SiteVisitRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final SiteVisitRepository   siteVisitRepository;
    private final OrderRepository       orderRepository;
    private final UserRepository        userRepository;
    private final ProductViewRepository productViewRepository;

    @Override
    @Transactional
    public void recordVisit(String visitorId, String ipHash) {
        LocalDate today = LocalDate.now();
        if (!siteVisitRepository.existsByVisitorIdAndVisitDate(visitorId, today)) {
            SiteVisit sv = new SiteVisit();
            sv.setVisitorId(visitorId);
            sv.setIpHash(ipHash);
            sv.setVisitDate(today);
            siteVisitRepository.save(sv);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StatsOverviewDto getOverview() {
        LocalDate today     = LocalDate.now();
        LocalDate weekAgo   = today.minusDays(6);
        LocalDate monthAgo  = today.minusDays(29);

        long todayVisitors  = siteVisitRepository.countUniqueVisitorsByDate(today);
        long weekVisitors   = siteVisitRepository.countUniqueVisitorsSince(weekAgo);
        long monthVisitors  = siteVisitRepository.countUniqueVisitorsSince(monthAgo);

        long todayOrders    = countOrdersSince(today.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
        long weekOrders     = countOrdersSince(weekAgo.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
        long monthOrders    = countOrdersSince(monthAgo.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));

        BigDecimal todayRevenue  = revenueSince(today.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
        BigDecimal weekRevenue   = revenueSince(weekAgo.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
        BigDecimal monthRevenue  = revenueSince(monthAgo.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));

        long todayNewUsers  = countUsersSince(today.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
        long weekNewUsers   = countUsersSince(weekAgo.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
        long monthNewUsers  = countUsersSince(monthAgo.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));

        long totalUsers     = userRepository.count();
        long totalOrders    = orderRepository.count();
        BigDecimal totalRev = orderRepository.sumTotalRevenue();

        return new StatsOverviewDto(
                todayVisitors, weekVisitors, monthVisitors,
                todayOrders,   weekOrders,   monthOrders,
                todayRevenue,  weekRevenue,  monthRevenue,
                todayNewUsers, weekNewUsers, monthNewUsers,
                totalUsers, totalOrders, totalRev == null ? BigDecimal.ZERO : totalRev
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DailyStatsDto getDaily(int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1);

        // Visitors per day
        Map<String, Long> visitorMap = new LinkedHashMap<>();
        for (Object[] row : siteVisitRepository.findDailyVisitors(from)) {
            visitorMap.put((String) row[0], ((Number) row[1]).longValue());
        }

        // Orders per day
        Map<String, long[]> orderMap = new LinkedHashMap<>();
        for (Object[] row : orderRepository.findDailyOrderStats(from.atStartOfDay().toInstant(java.time.ZoneOffset.UTC))) {
            String date   = (String) row[0];
            long count    = ((Number) row[1]).longValue();
            BigDecimal rev = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            orderMap.put(date, new long[]{count, rev.longValue()});
        }

        // New users per day
        Map<String, Long> userMap = new LinkedHashMap<>();
        for (Object[] row : userRepository.findDailyNewUsers(from.atStartOfDay().toInstant(java.time.ZoneOffset.UTC))) {
            userMap.put((String) row[0], ((Number) row[1]).longValue());
        }

        // Build aligned lists for each day in range
        List<String>     dates    = new ArrayList<>();
        List<Long>       visitors = new ArrayList<>();
        List<Long>       orders   = new ArrayList<>();
        List<BigDecimal> revenue  = new ArrayList<>();
        List<Long>       newUsers = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            String d = from.plusDays(i).toString();
            dates.add(d);
            visitors.add(visitorMap.getOrDefault(d, 0L));
            long[] od = orderMap.getOrDefault(d, new long[]{0L, 0L});
            orders.add(od[0]);
            revenue.add(BigDecimal.valueOf(od[1]));
            newUsers.add(userMap.getOrDefault(d, 0L));
        }

        return new DailyStatsDto(dates, visitors, orders, revenue, newUsers);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatusStatsDto getOrderStatusStats() {
        List<String> statuses = new ArrayList<>();
        List<Long>   counts   = new ArrayList<>();
        for (Object[] row : orderRepository.findOrderStatusCounts()) {
            statuses.add((String) row[0]);
            counts.add(((Number) row[1]).longValue());
        }
        return new OrderStatusStatsDto(statuses, counts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopProductDto> getTopViewedProducts(int limit) {
        return productViewRepository
                .findTopViewedProducts(PageRequest.of(0, limit))
                .stream()
                .map(row -> new TopProductDto(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue()
                ))
                .toList();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private long countOrdersSince(java.time.Instant from) {
        Long c = orderRepository.countOrdersSince(from);
        return c == null ? 0 : c;
    }

    private BigDecimal revenueSince(java.time.Instant from) {
        BigDecimal r = orderRepository.sumRevenueSince(from);
        return r == null ? BigDecimal.ZERO : r;
    }

    private long countUsersSince(java.time.Instant from) {
        Long c = userRepository.countNewUsersSince(from);
        return c == null ? 0 : c;
    }
}
