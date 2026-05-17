package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.CarouselItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarouselItemRepository extends JpaRepository<CarouselItem, Long> {

    List<CarouselItem> findByActiveTrueOrderByDisplayOrderAscIdAsc();

    List<CarouselItem> findAllByOrderByDisplayOrderAscIdAsc();
}
