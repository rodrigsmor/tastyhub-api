package com.rodrigo.tastyhub.modules.collections.infrastructure.persistence;

import com.rodrigo.tastyhub.modules.collections.application.dto.request.ListCollectionQuery;
import com.rodrigo.tastyhub.modules.collections.domain.model.CollectionVisibility;
import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class CollectionSpecifications {
    public static Specification<UserCollection> withFilters(Long targetUserId, Long requesterId, ListCollectionQuery criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("user").get("id"), targetUserId));

            if (StringUtils.hasText(criteria.query())) {
                String searchPattern = "%" + criteria.query().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), searchPattern),
                    cb.like(cb.lower(root.get("description")), searchPattern)
                ));
            }

            boolean isOwner = requesterId != null && requesterId.equals(targetUserId);

            if (!isOwner) {
                predicates.add(cb.equal(root.get("isPublic"), true));

                if (criteria.visibility() == CollectionVisibility.PRIVATE) {
                    return cb.disjunction();
                }
            } else {
                if (criteria.visibility() != CollectionVisibility.ALL) {
                    boolean isPublicSearch = criteria.visibility() == CollectionVisibility.PUBLIC;
                    predicates.add(cb.equal(root.get("isPublic"), isPublicSearch));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}