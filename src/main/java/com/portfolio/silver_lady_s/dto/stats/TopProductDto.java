package com.portfolio.silver_lady_s.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopProductDto {
    private Long   id;
    private String name;
    private String imageUrl;
    private long   totalViews;
}
