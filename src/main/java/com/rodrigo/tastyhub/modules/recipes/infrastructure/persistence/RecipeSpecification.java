package com.rodrigo.tastyhub.modules.recipes.infrastructure.persistence;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.ListRecipesQuery;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RecipeSpecification {
    public static Specification<Recipe> withFilters(ListRecipesQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.categories() != null && !query.categories().isEmpty()) {
                predicates.add(root.get("category").in(query.categories()));
            }

            if (query.minRating() != null || query.maxRating() != null) {
                var stats = root.get("statistics");

                if (query.minRating() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(stats.get("averageRating"), query.minRating()));
                }

                if (query.maxRating() != null) {
                    predicates.add(cb.lessThanOrEqualTo(stats.get("averageRating"), query.maxRating()));
                }
            }

            if (query.minRating() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("averageRating"),
                        query.minRating()
                    )
                );
            }

            if (query.tags() != null && !query.tags().isEmpty()) {
                Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
                Root<Recipe> subRoot = subquery.from(Recipe.class);
                Join<Object, Object> subTags = subRoot.join("tags");

                subquery.select(subRoot.get("id"))
                    .where(
                        cb.and(
                            cb.equal(subRoot.get("id"), root.get("id")),
                            subTags.get("name").in(query.tags())
                        )
                    );

                predicates.add(cb.exists(subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
