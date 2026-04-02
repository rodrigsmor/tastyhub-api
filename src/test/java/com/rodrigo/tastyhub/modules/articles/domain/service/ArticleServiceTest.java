package com.rodrigo.tastyhub.modules.articles.domain.service;

import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.articles.domain.repository.ArticleRepository;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class ArticleServiceTest {
    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleService articleService;

    private Article fakeArticle;

    @BeforeEach
    void setup() {
        fakeArticle = new Article(
            "Fake Article title",
            "Praesent in imperdiet leo, feugiat placerat orci...",
            new User(),
            true,
            "en-US"
        );
        fakeArticle.setId(100L);
    }

    @Nested
    @DisplayName("Tests for countByAuthorId")
    class CountByAuthorIdTests {
        @Test
        @DisplayName("Should return the correct count of articles for a given author")
        void shouldReturnCountSuccessfully() {
            Long authorId = 1L;
            when(articleRepository.countByAuthorId(authorId)).thenReturn(5L);

            Long count = articleService.countByAuthorId(authorId);

            assertEquals(5L, count);
            verify(articleRepository, times(1)).countByAuthorId(authorId);
        }

        @Test
        @DisplayName("Should return zero if author has no articles")
        void shouldReturnZeroIfNoArticlesFound() {
            when(articleRepository.countByAuthorId(anyLong())).thenReturn(0L);

            Long count = articleService.countByAuthorId(99L);

            assertEquals(0L, count);
        }
    }

    @Nested
    @DisplayName("Tests for findByIdOrThrow")
    class FindByIdOrThrowTests {
        @Test
        @DisplayName("Should return article when a valid ID is provided")
        void shouldReturnArticleWhenIdExists() {
            Long articleId = 100L;
            when(articleRepository.findById(articleId)).thenReturn(Optional.of(fakeArticle));

            Article result = articleService.findByIdOrThrow(articleId);

            assertNotNull(result);
            assertEquals(articleId, result.getId());
            assertEquals("Fake Article title", result.getTitle());
            verify(articleRepository).findById(articleId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when article does not exist")
        void shouldThrowExceptionWhenArticleNotFound() {
            Long invalidId = 999L;
            when(articleRepository.findById(invalidId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                articleService.findByIdOrThrow(invalidId)
            );

            assertEquals("Article not found with the provided ID", exception.getMessage());
            verify(articleRepository).findById(invalidId);
        }
    }
}