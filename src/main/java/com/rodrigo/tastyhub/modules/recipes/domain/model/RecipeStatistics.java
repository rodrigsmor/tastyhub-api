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

    @Builder.Default
    @Column(name = "favorites_count", nullable = false)
    private Integer favoritesCount = 0;

    @Builder.Default
    @Column(name = "reviews_count", nullable = false)
    private Integer reviewsCount = 0;

    @Builder.Default
    @Column(name = "total_rating_sum", nullable = false)
    private Integer totalRatingSum = 0;

    @Builder.Default
    @Column(name = "average_rating", nullable = false)
    private Double averageRating = 0.0;
}
