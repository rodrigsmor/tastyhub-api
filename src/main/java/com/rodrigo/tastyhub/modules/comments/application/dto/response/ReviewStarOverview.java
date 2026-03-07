package com.rodrigo.tastyhub.modules.comments.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Percentage and count breakdown for a specific rating value")
public record ReviewStarOverview(
    @Schema(description = "The rating value (stars)", example = "5")
    int ratingValue,

    @Schema(description = "Percentage of total reviews with this rating", example = "75.5")
    double percentage,

    @Schema(description = "Total count of reviews with this rating", example = "140")
    int count
) {}