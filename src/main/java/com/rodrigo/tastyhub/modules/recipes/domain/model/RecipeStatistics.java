package com.rodrigo.tastyhub.modules.recipes.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @Version
    private Long version;

    public void incrementRating(BigDecimal newRating) {
        this.reviewsCount++;
        this.totalRatingSum += newRating.intValue();
        this.averageRating = (double) this.totalRatingSum / this.reviewsCount;
    }

    public void decrementRating(BigDecimal newRating) {
        this.reviewsCount--;
        this.totalRatingSum -= newRating.intValue();
        this.averageRating = (double) this.totalRatingSum / this.reviewsCount;
    }

    public void incrementFavoritesCount() {
        this.favoritesCount++;
    }

    public void decrementFavoritesCount() {
        if (this.favoritesCount > 0) {
            this.favoritesCount--;
        }
    }
}
