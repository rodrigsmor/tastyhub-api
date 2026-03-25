package com.rodrigo.tastyhub.modules.articles.application.dto.response;

import com.rodrigo.tastyhub.modules.articles.domain.model.ArticleSortBy;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Parameters for filtering and paginating articles")
public record ListArticlesQuery(
    @Schema(description = "Search by title or description", example = "best practices of cooking")
    String query,

    @Min(0)
    @Schema(description = "Page number (0-indexed)", defaultValue = "0")
    Integer page,

    @Min(1)
    @Max(100)
    @Schema(description = "Number of items per page", defaultValue = "10")
    Integer size,

    @Schema(description = "Field to sort by", defaultValue = "CREATED_AT")
    ArticleSortBy sortBy,

    @Schema(description = "Order direction", defaultValue = "DESC")
    SortDirection direction
) {
    public ListArticlesQuery {
        page = (page == null) ? 0 : page;
        size = (size == null) ? 10 : size;
        sortBy = (sortBy == null) ? ArticleSortBy.CREATED_AT : sortBy;
        direction = (direction == null) ? SortDirection.DESC : direction;
    }
}
