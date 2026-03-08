package com.rodrigo.tastyhub.modules.comments.domain.repository;

import com.rodrigo.tastyhub.modules.comments.domain.model.Comment;
import com.rodrigo.tastyhub.modules.comments.domain.model.ReviewStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
    @Query(
        "SELECT " +
            "COUNT(DISTINCT c.user.id) AS totalUsers, " +
            "COUNT(c) AS totalReviews, " +
            "AVG(c.rating) AS averageRating " +
        "FROM Comment c " +
        "WHERE c.recipe.id = :recipeId"
    )
    ReviewStatsProjection getReviewStatsByRecipeId(@Param("recipeId") Long recipeId);

    @Query(
        "SELECT FLOOR(c.rating) AS ratingValue, COUNT(c) AS count " +
        "FROM Comment c " +
        "WHERE c.recipe.id = :recipeId " +
        "GROUP BY FLOOR(c.rating)"
    )
    List<Map<String, Object>> getRatingCountBreakdown(@Param("recipeId") Long recipeId);
}
