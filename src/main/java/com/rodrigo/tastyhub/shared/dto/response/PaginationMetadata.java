package com.rodrigo.tastyhub.shared.dto.response;

import com.rodrigo.tastyhub.shared.enums.SortDirection;

public record PaginationMetadata(
    int page,
    int pageSize,
    int totalPages,
    long totalItems,
    SortDirection direction,
    boolean hasNext,
    boolean hasPrevious
) {}
