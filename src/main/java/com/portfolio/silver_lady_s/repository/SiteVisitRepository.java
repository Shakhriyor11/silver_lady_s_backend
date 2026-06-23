package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.SiteVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SiteVisitRepository extends JpaRepository<SiteVisit, Long> {

    boolean existsByVisitorIdAndVisitDate(String visitorId, LocalDate visitDate);

    @Query(value = """
            SELECT visit_date::text AS date,
                   COUNT(DISTINCT visitor_id) AS count
            FROM site_visits
            WHERE visit_date >= :from
            GROUP BY visit_date
            ORDER BY visit_date
            """, nativeQuery = true)
    List<Object[]> findDailyVisitors(@Param("from") LocalDate from);

    @Query(value = "SELECT COUNT(DISTINCT visitor_id) FROM site_visits WHERE visit_date = :date",
           nativeQuery = true)
    long countUniqueVisitorsByDate(@Param("date") LocalDate date);

    @Query(value = "SELECT COUNT(DISTINCT visitor_id) FROM site_visits WHERE visit_date >= :from",
           nativeQuery = true)
    long countUniqueVisitorsSince(@Param("from") LocalDate from);
}
