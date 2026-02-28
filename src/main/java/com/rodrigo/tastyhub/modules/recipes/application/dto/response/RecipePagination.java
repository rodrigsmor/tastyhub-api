package com.rodrigo.tastyhub.modules.recipes.application.dto.response;

import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;

import java.util.List;

public record RecipePagination(
    List<SummaryRecipeDto> recipes,
    PaginationMetadata metadata
) {}
