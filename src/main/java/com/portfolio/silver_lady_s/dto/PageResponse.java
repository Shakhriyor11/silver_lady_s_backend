package com.portfolio.silver_lady_s.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Barcha pagination javoblari uchun umumiy wrapper.
 * Frontend bir xil struktura kutadi.
 */
@Getter
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;

    public PageResponse(Page<T> page) {
        this.content       = page.getContent();
        this.page          = page.getNumber();
        this.size          = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages    = page.getTotalPages();
        this.last          = page.isLast();
    }
}
