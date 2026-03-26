package com.rodrigo.tastyhub.modules.articles.domain.service;

import com.rodrigo.tastyhub.modules.articles.application.dto.response.ArticlePaginationDto;
import com.rodrigo.tastyhub.modules.articles.application.dto.response.ListArticlesQuery;
import com.rodrigo.tastyhub.modules.articles.application.dto.response.SummaryArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.mapper.ArticleMapper;
import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.articles.domain.model.ArticleSortBy;
import com.rodrigo.tastyhub.modules.articles.domain.repository.ArticleRepository;
import com.rodrigo.tastyhub.modules.articles.infrastructure.persistence.ArticleSpecification;
import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    public Long countByAuthorId(Long authorId) {
        return this.articleRepository.countByAuthorId(authorId);
    }

    public Article findByIdOrThrow(Long recipeId) {
        return articleRepository.findById(recipeId)
            .orElseThrow(() -> new ResourceNotFoundException("Article not found with the provided ID"));
    }

    public ArticlePaginationDto findAll(
        ListArticlesQuery request,
        @Nullable User owner,
        @Nullable UserCollection collection
    ) {
        Pageable pageable = PageRequest.of(
            request.page(),
            request.size(),
            buildSort(request.sortBy(), request.direction())
        );

        Page<Article> page = articleRepository.findAll(
            ArticleSpecification.withFilters(
                request,
                collection == null ? null : collection.getId(),
                owner != null ? owner.getId() : null
            ),
            pageable
        );

        List<SummaryArticleDto> articles = page.getContent()
            .stream()
            .map(ArticleMapper::toSummary)
            .toList();

        PaginationMetadata metadata = new PaginationMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            request.direction(),
            page.hasNext(),
            page.hasPrevious()
        );

        return new ArticlePaginationDto(articles, metadata);
    }

    public Article create(
        String title,
        String content,
        Boolean isPublic,
        String language,
        User author
    ) {
        Article article = new Article(
            title,
            content,
            author,
            isPublic,
            language
        );

        return articleRepository.save(article);
    }

    @Transactional
    private Sort buildSort(ArticleSortBy sortBy, SortDirection direction) {
        String field = switch (sortBy) {
            case TITLE -> "title";
            case RELEVANCE -> "statistics.likesCount";
            case LIKES -> "statistics.favoritesCount";
            default -> "createdAt";
        };

        return direction == SortDirection.ASC
            ? Sort.by(field).ascending()
            : Sort.by(field).descending();
    }
}
