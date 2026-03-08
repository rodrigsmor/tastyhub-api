package com.rodrigo.tastyhub.modules.collections.application.dto.request;

import com.rodrigo.tastyhub.modules.collections.domain.model.CollectionSortBy;
import com.rodrigo.tastyhub.modules.collections.domain.model.CollectionVisibility;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Parameters for filtering and paginating user collections")
public record ListCollectionQuery(
    @Schema(description = "Text search for collection name or description", example = "Pasta")
    String query,

    @Min(value = 0, message = "Page index must be 0 or greater")
    @Schema(description = "Page number (0-indexed)", example = "0")
    Integer page,

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    @Schema(description = "Number of items per page", example = "20")
    Integer size,

    @Schema(description = "Field to sort the results by", example = "CREATED_AT")
    CollectionSortBy sortBy,

    @Schema(description = "Direction of the sort", example = "DESC")
    SortDirection direction,

    @Schema(description = "Visibility filter for the collections", example = "PUBLIC")
    CollectionVisibility visibility
) {
    public ListCollectionQuery {
        page = (page == null) ? 0 : page;
        size = (size == null) ? 20 : size;
        direction = (direction == null) ? SortDirection.DESC : direction;
        visibility = (visibility == null) ? CollectionVisibility.ALL : visibility;
    }
}
