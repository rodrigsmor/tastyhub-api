package com.rodrigo.tastyhub.modules.comments.infrastructure.persistence;

import com.rodrigo.tastyhub.modules.comments.domain.model.Comment;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class CommentSpecification {
    public static Specification<Comment> withFilters(Long recipeId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (recipeId != null) {
                predicates.add(cb.equal(root.get("recipe").get("id"), recipeId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
