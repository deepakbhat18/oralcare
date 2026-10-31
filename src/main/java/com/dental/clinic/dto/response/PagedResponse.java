package com.dental.clinic.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;









@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {

    private List<T> content;
    private int     pageNumber;
    private int     pageSize;
    private long    totalElements;
    private int     totalPages;
    private boolean last;
    private boolean first;

    public static <T> PagedResponse<T> of(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}

/*
 * This file contains the PagedResponse class which manages generic helper tasks and clinical administration configurations.
 * Concepts used: Java object-oriented design and clean architecture.
 */
