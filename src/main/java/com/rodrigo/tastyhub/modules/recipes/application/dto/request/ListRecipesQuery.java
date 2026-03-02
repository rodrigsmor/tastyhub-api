package com.rodrigo.tastyhub.modules.recipes.application.dto.request;

import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeCategory;
import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeSortBy;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Parameters for filtering and paginating recipes")
public record ListRecipesQuery(
    @Schema(description = "Search by title or description", example = "Chocolate Cake")
    String query,

    @Min(0)
    @Schema(description = "Page number (0-indexed)", defaultValue = "0")
    Integer page,

    @Min(1)
    @Max(100)
    @Schema(description = "Number of items per page", defaultValue = "10")
    Integer size,

    @Schema(description = "Field to sort by", defaultValue = "CREATED_AT")
    RecipeSortBy sortBy,

    @Schema(description = "Order direction", defaultValue = "DESC")
    SortDirection direction,

    @Schema(description = "Filter by tag names", example = "[\"Vegan\", \"Easy\"]")
    List<String> tags,

    @Schema(description = "Filter by recipe categories")
    List<RecipeCategory> categories,

    @Schema(description = "Filter by specific ingredients")
    List<String> ingredients,

    @Schema(description = "Filter by currency codes", example = "[\"USD\", \"BRL\"]")
    List<String> currencies,

    @DecimalMin("0.0") @DecimalMax("5.0")
    @Schema(description = "Minimum average rating (0 to 5)", example = "4.5")
    Double minRating,

    @DecimalMin("0.0") @DecimalMax("5.0")
    @Schema(description = "Maximum average rating (0 to 5)", example = "5.0")
    Double maxRating,

    @PositiveOrZero
    @Schema(description = "Minimum estimated cost")
    Integer minCost,

    @PositiveOrZero
    @Schema(description = "Maximum estimated cost")
    Integer maxCost,

    @PositiveOrZero
    @Schema(description = "Minimum number of ingredients")
    Integer minIngredients,

    @PositiveOrZero
    @Schema(description = "Maximum number of ingredients")
    Integer maxIngredients
) {
    public ListRecipesQuery {
        page = (page == null) ? 0 : page;
        size = (size == null) ? 10 : size;
        sortBy = (sortBy == null) ? RecipeSortBy.CREATION_DATE : sortBy;
        direction = (direction == null) ? SortDirection.DESC : direction;
    }
}
