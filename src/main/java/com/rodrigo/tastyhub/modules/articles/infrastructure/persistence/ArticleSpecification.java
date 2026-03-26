package com.rodrigo.tastyhub.modules.articles.infrastructure.persistence;

import com.rodrigo.tastyhub.modules.articles.application.dto.response.ListArticlesQuery;
import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.articles.domain.model.ArticleStatistics;
import com.rodrigo.tastyhub.modules.settings.domain.model.ProfileVisibility;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ArticleSpecification {
    public static Specification<Article> withFilters(ListArticlesQuery query, Long collectionId, Long currentUserId) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Article, User> authorJoin = root.join("author", JoinType.INNER);
            Join<Object, Object> settingsJoin = authorJoin.join("settings", JoinType.LEFT);

            Predicate isPublicArticle = cb.isTrue(root.get("isPublic"));
            Predicate authorNotPrivate = cb.notEqual(
                settingsJoin.get("profileVisibility"),
                ProfileVisibility.PRIVATE
            );
            Predicate visibleToAll = cb.and(isPublicArticle, authorNotPrivate);

            if (currentUserId != null) {
                Predicate isOwner = cb.equal(authorJoin.get("id"), currentUserId);
                predicates.add(cb.or(visibleToAll, isOwner));
            } else {
                predicates.add(visibleToAll);
            }

            if (query.query() != null && !query.query().isBlank()) {
                String keyword = "%" + query.query().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), keyword);
                Predicate contentLike = cb.like(cb.lower(root.get("content")), keyword);
                predicates.add(cb.or(titleLike, contentLike));
            }

            if (query.language() != null && !query.language().isBlank()) {
                predicates.add(cb.equal(root.get("language"), query.language()));
            }

            if (collectionId != null) {
                Join<Article, Object> collectionsJoin = root.join("collections");
                predicates.add(cb.equal(collectionsJoin.get("id"), collectionId));
            }

            if (
                query.minComments() != null || query.maxComments() != null ||
                query.minLikesCount() != null || query.maxLikesCount() != null
            ) {
                Join<Article, ArticleStatistics> statsJoin = root.join("statistics", JoinType.LEFT);

                if (query.minComments() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(statsJoin.get("commentsCount"), query.minComments()));
                }
                if (query.maxComments() != null) {
                    predicates.add(cb.lessThanOrEqualTo(statsJoin.get("commentsCount"), query.maxComments()));
                }

                if (query.minLikesCount() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(statsJoin.get("likesCount"), query.minLikesCount()));
                }
                if (query.maxLikesCount() != null) {
                    predicates.add(cb.lessThanOrEqualTo(statsJoin.get("likesCount"), query.maxLikesCount()));
                }
            }

            if (query.minCreatedAt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), query.minCreatedAt()));
            }
            if (query.maxCreatedAt() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), query.maxCreatedAt()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}