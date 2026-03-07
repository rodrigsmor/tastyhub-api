package com.rodrigo.tastyhub.modules.comments.application.dto.response;

import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;

import java.util.List;

public record ReviewPagination(
    List<ReviewResponseDto> reviews,
    ReviewSummaryDto summary,
    PaginationMetadata metadata
) {}
