package com.rodrigo.tastyhub.modules.articles.application.usecases;

import com.rodrigo.tastyhub.modules.articles.application.dto.response.ArticlePaginationDto;
import com.rodrigo.tastyhub.modules.articles.application.dto.response.ListArticlesQuery;
import com.rodrigo.tastyhub.modules.articles.application.dto.response.SummaryArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.mapper.ArticleMapper;
import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.articles.domain.service.ArticleService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListArticlesUseCase {
    private final SecurityService securityService;
    private final ArticleService articleService;

    public ArticlePaginationDto execute(ListArticlesQuery query) {
        User user = this.securityService.getCurrentUserOptional().orElse(null);

        Page<Article> page = this.articleService.findAll(query, user, null);

        List<SummaryArticleDto> articles = page.getContent()
            .stream()
            .map(ArticleMapper::toSummary)
            .toList();

        PaginationMetadata metadata = new PaginationMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            query.direction(),
            page.hasNext(),
            page.hasPrevious()
        );

        return new ArticlePaginationDto(articles, metadata);
    }
}
