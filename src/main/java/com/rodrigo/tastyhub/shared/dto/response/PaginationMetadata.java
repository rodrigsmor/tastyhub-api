package com.rodrigo.tastyhub.shared.dto.response;

import com.rodrigo.tastyhub.shared.enums.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Metadata for paginated responses, providing navigation details.")
public record PaginationMetadata(
    @Schema(description = "Current page number (starting from 0)", example = "0")
    int page,

    @Schema(description = "Number of items per page", example = "10")
    int pageSize,

    @Schema(description = "Total number of pages available", example = "5")
    int totalPages,

    @Schema(description = "Total number of items across all pages", example = "48")
    long totalItems,

    @Schema(description = "Sort direction of the results")
    SortDirection direction,

    @Schema(description = "Indicates if there is a next page available", example = "true")
    boolean hasNext,

    @Schema(description = "Indicates if there is a previous page available", example = "false")
    boolean hasPrevious
) {}
