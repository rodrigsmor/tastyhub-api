package com.rodrigo.tastyhub.modules.comments.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Summary of all ratings and reviews for a recipe")
public record ReviewSummaryDto(
    @Schema(description = "Total number of unique users who rated this recipe", example = "150")
    int totalUsers,

    @Schema(description = "Total number of reviews submitted", example = "185")
    int totalReviews,

    @Schema(description = "Average rating score from 1 to 5", example = "4.7")
    double averageRating,

    @Schema(description = "Detailed breakdown of ratings by star level")
    List<ReviewStarOverview> ratingBreakdown
) {}
