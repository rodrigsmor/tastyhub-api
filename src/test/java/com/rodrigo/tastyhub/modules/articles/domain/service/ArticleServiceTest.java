package com.rodrigo.tastyhub.modules.articles.domain.service;

import com.rodrigo.tastyhub.modules.articles.application.dto.response.ListArticlesQuery;
import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.articles.domain.model.ArticleSortBy;
import com.rodrigo.tastyhub.modules.articles.domain.repository.ArticleRepository;
import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
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

    @Nested
    @DisplayName("Tests for findAll (Articles with Pagination and Filters)")
    class FindAllArticlesTests {
        @Test
        @DisplayName("Should call repository with correct pagination and specifications")
        void shouldCallRepositoryWithCorrectParameters() {
            ListArticlesQuery query = new ListArticlesQuery(
                "cooking",
                0,
                10,
                ArticleSortBy.CREATED_AT,
                SortDirection.DESC,
                "en-US",
                null,
                null,
                null,
                null,
                null,
                null
            );

            User owner = new User();
            owner.setId(1L);

            UserCollection collection = new UserCollection();
            collection.setId(50L);

            Page<Article> expectedPage = new PageImpl<>(List.of(fakeArticle));

            when(articleRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(expectedPage);

            Page<Article> result = articleService.findAll(query, owner, collection);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(articleRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(0, capturedPageable.getPageNumber());
            assertEquals(10, capturedPageable.getPageSize());
            assertTrue(capturedPageable.getSort().getOrderFor("createdAt").isDescending());
        }

        @Test
        @DisplayName("Should handle null owner and collection gracefully")
        void shouldHandleNullContexts() {
            ListArticlesQuery query = new ListArticlesQuery(
                null,
                0,
                10,
                ArticleSortBy.TITLE,
                SortDirection.ASC,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );

            when(articleRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

            articleService.findAll(query, null, null);

            verify(articleRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Tests for create Article")
    class CreateArticleTests {
        @Test
        @DisplayName("Should successfully create and save an article")
        void shouldCreateArticleSuccessfully() {
            String title = "New Article";
            String content = "Full content of the article...";
            User author = new User();
            author.setId(1L);
            String language = "pt-BR";
            Boolean isPublic = true;

            when(articleRepository.save(any(Article.class))).thenAnswer(i -> i.getArgument(0));

            Article result = articleService.create(title, content, isPublic, language, author);

            assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(title, result.getTitle()),
                () -> assertEquals(content, result.getContent()),
                () -> assertEquals(author, result.getAuthor()),
                () -> assertEquals(language, result.getLanguage()),
                () -> assertTrue(result.isPublic())
            );

            verify(articleRepository, times(1)).save(any(Article.class));
        }

        @Test
        @DisplayName("Should throw NullPointerException when required fields are missing")
        void shouldThrowExceptionWhenRequiredFieldsAreMissing() {
            User author = new User();

            assertThrows(NullPointerException.class, () ->
                articleService.create(null, "content", true, "en-US", author)
            );

            assertThrows(NullPointerException.class, () ->
                articleService.create("Title", null, true, "en-US", author)
            );

            verify(articleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should use default visibility (true) when isPublic is null")
        void shouldDefaultToPublicWhenIsPublicIsNull() {
            when(articleRepository.save(any(Article.class))).thenAnswer(i -> i.getArgument(0));

            Article result = articleService.create("Title", "Content", null, "en-US", new User());

            assertTrue(result.isPublic(), "Article should be public by default if isPublic is null");
        }
    }

    @Nested
    @DisplayName("Tests for update Article")
    class UpdateArticleTests {
        @Test
        @DisplayName("Should successfully update article when owner is valid and fields are provided")
        void shouldUpdateArticleSuccessfully() {
            Long articleId = 100L;
            Long ownerId = 1L;
            String newTitle = "Updated Title";

            User author = new User();
            author.setId(ownerId);
            fakeArticle.setAuthor(author);

            when(articleRepository.findById(articleId)).thenReturn(Optional.of(fakeArticle));
            when(articleRepository.save(any(Article.class))).thenAnswer(i -> i.getArgument(0));

            Article result = articleService.update(
                articleId,
                newTitle,
                "New content",
                false,
                "pt-BR",
                ownerId
            );

            assertEquals(newTitle, result.getTitle());
            assertFalse(result.isPublic());
            assertEquals("pt-BR", result.getLanguage());
            verify(articleRepository).save(fakeArticle);
        }

        @Test
        @DisplayName("Should throw ForbiddenException when a different user tries to update")
        void shouldThrowForbiddenWhenNotOwner() {
            Long articleId = 100L;
            Long hackerId = 999L;

            User author = new User();
            author.setId(1L);
            fakeArticle.setAuthor(author);

            when(articleRepository.findById(articleId)).thenReturn(Optional.of(fakeArticle));

            assertThrows(ForbiddenException.class, () ->
                articleService.update(
                    articleId,
                "Title",
                    "Content",
                    true,
                    "en-US",
                    hackerId
                )
            );

            verify(articleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DomainException when provided fields are blank")
        void shouldThrowExceptionWhenFieldsAreBlank() {
            Long ownerId = 1L;
            User author = new User();
            author.setId(ownerId);
            fakeArticle.setAuthor(author);

            when(articleRepository.findById(100L)).thenReturn(Optional.of(fakeArticle));

            assertThrows(DomainException.class, () ->
                articleService.update(
                    100L,
                    "   ",
                    "content",
                    true,
                    "en-US",
                    ownerId
                )
            );
        }
    }

    @Nested
    @DisplayName("Tests for update Article Cover")
    class UpdateCoverTests {
        @Test
        @DisplayName("Should successfully update cover and alt text when owner is valid")
        void shouldUpdateCoverSuccessfully() {
            Long articleId = 100L;
            Long ownerId = 1L;
            String newUrl = "https://images.com/new-cover.jpg";
            String newAlt = "A beautiful food shot";

            User author = new User();
            author.setId(ownerId);
            fakeArticle.setAuthor(author);

            when(articleRepository.findById(articleId)).thenReturn(Optional.of(fakeArticle));
            when(articleRepository.save(any(Article.class))).thenAnswer(i -> i.getArgument(0));

            Article result = articleService.updateCover(articleId, newUrl, newAlt, ownerId);

            assertEquals(newUrl, result.getCoverUrl());
            assertEquals(newAlt, result.getCoverAlt());
            verify(articleRepository).save(fakeArticle);
        }

        @Test
        @DisplayName("Should throw ForbiddenException when a non-author tries to update the cover")
        void shouldThrowForbiddenWhenNotAuthor() {
            Long articleId = 100L;
            Long hackerId = 999L;

            User author = new User();
            author.setId(1L);
            fakeArticle.setAuthor(author);

            when(articleRepository.findById(articleId)).thenReturn(Optional.of(fakeArticle));

            assertThrows(ForbiddenException.class, () ->
                articleService.updateCover(
                    articleId,
                    "url",
                    "alt",
                    hackerId
                )
            );

            verify(articleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should clear alt text if the cover URL is set to null")
        void shouldClearAltTextWhenUrlIsNull() {
            Long ownerId = 1L;
            User author = new User();
            author.setId(ownerId);
            fakeArticle.setAuthor(author);
            fakeArticle.setCoverAlt("Old Alt Text");

            when(articleRepository.findById(100L)).thenReturn(Optional.of(fakeArticle));
            when(articleRepository.save(any(Article.class))).thenAnswer(i -> i.getArgument(0));

            Article result = articleService.updateCover(
                100L,
                null,
                "New Alt",
                ownerId
            );

            assertNull(result.getCoverUrl());
            assertNull(result.getCoverAlt(), "Alt text should be null if URL is null according to business rule");
        }
    }

    @Nested
    @DisplayName("Tests for delete Article")
    class DeleteArticleTests {
        @Test
        @DisplayName("Should successfully delete article when owner is valid")
        void shouldDeleteArticleSuccessfully() {
            Long articleId = 100L;
            Long ownerId = 1L;

            User author = new User();
            author.setId(ownerId);
            fakeArticle.setAuthor(author);

            when(articleRepository.findById(articleId)).thenReturn(Optional.of(fakeArticle));

            articleService.delete(articleId, ownerId);

            verify(articleRepository, times(1)).delete(fakeArticle);
        }

        @Test
        @DisplayName("Should throw ForbiddenException when a non-author tries to delete")
        void shouldThrowForbiddenWhenNotOwner() {
            Long articleId = 100L;
            Long hackerId = 999L;

            User author = new User();
            author.setId(1L);
            fakeArticle.setAuthor(author);

            when(articleRepository.findById(articleId)).thenReturn(Optional.of(fakeArticle));

            assertThrows(ForbiddenException.class, () ->
                articleService.delete(articleId, hackerId)
            );

            verify(articleRepository, never()).delete(any(Article.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when trying to delete non-existent article")
        void shouldThrowNotFoundWhenArticleDoesNotExist() {
            Long invalidId = 404L;
            when(articleRepository.findById(invalidId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                articleService.delete(invalidId, 1L)
            );

            verify(articleRepository, never()).delete((Article) any());
        }
    }
}