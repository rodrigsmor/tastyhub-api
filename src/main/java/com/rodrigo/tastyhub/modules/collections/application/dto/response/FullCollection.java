package com.rodrigo.tastyhub.modules.collections.application.dto.response;

import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.SummaryRecipeDto;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;

import java.util.List;

public record FullCollection(
    UserCollectionResponseDto collection,
    List<SummaryRecipeDto> recipes,
    List<Article> articles,
    PaginationMetadata metadata
) { }
