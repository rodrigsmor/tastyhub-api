package com.rodrigo.tastyhub.modules.articles.infrastructure.persistence;

import com.rodrigo.tastyhub.modules.articles.application.dto.response.ListArticlesQuery;
import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.settings.domain.model.ProfileVisibility;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ArticleSpecification {
    public static Specification<Article> withFilters(ListArticlesQuery query, Long collectionId, Long currentUserId) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Recipe, Object> authorJoin = root.join("author", JoinType.LEFT);
            Join<Object, Object> settingsJoin = authorJoin.join("settings", JoinType.LEFT);

            Predicate isPublicRecipe = cb.isTrue(root.get("isPublic"));

            Predicate authorNotPrivate = cb.notEqual(
                settingsJoin.get("profileVisibility"),
                ProfileVisibility.PRIVATE
            );

            Predicate visibleToAll = cb.and(isPublicRecipe, authorNotPrivate);

            if (currentUserId != null) {
                Predicate isOwner = cb.equal(authorJoin.get("id"), currentUserId);
                predicates.add(cb.or(visibleToAll, isOwner));
            } else {
                predicates.add(visibleToAll);
            }

            if (collectionId != null) {
                Join<Recipe, Object> collectionsJoin = root.join("collections");
                predicates.add(cb.equal(collectionsJoin.get("id"), collectionId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
