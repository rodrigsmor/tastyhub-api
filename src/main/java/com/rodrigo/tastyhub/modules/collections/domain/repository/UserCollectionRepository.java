package com.rodrigo.tastyhub.modules.collections.domain.repository;

import com.rodrigo.tastyhub.modules.collections.application.dto.response.CollectionCounts;
import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCollectionRepository extends JpaRepository<UserCollection, Long>, JpaSpecificationExecutor<UserCollection> {
    @Query("""
        SELECT
            (SELECT COUNT(r) FROM c.recipes r) AS recipeCounts,
            (SELECT COUNT(a) FROM c.articles a) AS articleCounts
        FROM UserCollection c
        WHERE c.id = :collectionId
    """)
    Optional<CollectionCounts> getCollectionCountsById(@Param("collectionId") Long collectionId);
}
