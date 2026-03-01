package com.rodrigo.tastyhub.modules.recipes.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "recipe_statistics")
public class RecipeStatistics {
    @Id
    private Long recipeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Column(name = "favorites_count", nullable = false)
    private Integer favoritesCount = 0;

    @Column(name = "reviews_count", nullable = false)
    private Integer reviewsCount = 0;

    @Column(name = "total_rating_sum", nullable = false)
    private Integer totalRatingSum = 0;

    @Column(name = "average_rating", nullable = false)
    private Double averageRating = 0.0;
}
