package com.rodrigo.tastyhub.modules.user.domain.projections;

import com.rodrigo.tastyhub.modules.settings.domain.model.ProfileVisibility;

import java.time.LocalDate;

public interface UserProfileProjection {
    Long getId();
    String getFirstName();
    String getLastName();
    String getUsername();
    String getProfilePictureUrl();
    String getProfilePictureAlt();
    String getBio();
    String getCoverUrl();
    String getCoverAlt();
    ProfileVisibility getVisibility();
    LocalDate getDateOfBirth();
    Long getRecipeCount();
    Long getArticleCount();
    Long getFollowerCount();
    Long getFollowingCount();
    Boolean getIsFollowing();
    Boolean getIsFollower();
}
