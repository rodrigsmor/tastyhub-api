package com.rodrigo.tastyhub.modules.recipes.application.dto.request;

import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeSortBy;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public record ListRecipesRequest(
    @Min(0)
    Integer page,
    @Max(100)
    Integer size,
    RecipeSortBy sortBy,
    SortDirection direction,
    List<String> tags,
    List<String> languages,
    List<String> categories,
    List<String> ingredients,
    List<String> currencies,
    Double minRating,
    Double maxRating,
    Integer minCost,
    Integer maxCost,
    Integer minIngredients,
    Integer maxIngredients
) {}
