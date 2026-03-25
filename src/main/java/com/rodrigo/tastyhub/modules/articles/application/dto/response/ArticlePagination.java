package com.rodrigo.tastyhub.modules.articles.application.dto.response;

import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;

import java.util.List;

public record ArticlePagination(
    List<SummaryArticleDto> articles,
    PaginationMetadata metadata
) {}
