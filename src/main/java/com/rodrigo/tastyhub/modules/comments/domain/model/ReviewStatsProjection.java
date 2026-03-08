package com.rodrigo.tastyhub.modules.comments.domain.model;

public interface ReviewStatsProjection {
    int getTotalUsers();
    int getTotalReviews();
    Double getAverageRating();
}
