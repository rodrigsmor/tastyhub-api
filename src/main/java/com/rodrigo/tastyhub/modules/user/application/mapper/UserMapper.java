package com.rodrigo.tastyhub.modules.user.application.mapper;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserFullStatsDto;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserProfileDto;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import com.rodrigo.tastyhub.modules.user.domain.model.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserSummaryDto toSummary(User user) {
        return new UserSummaryDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getUsername(),
            user.getProfilePictureUrl(),
            user.getProfilePictureAlt()
        );
    }

    public static UserProfileDto toProfile(User user) {
        return new UserProfileDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getUsername(),
            user.getProfilePictureUrl(),
            user.getProfilePictureAlt(),
            user.getBio(),
            user.getCoverPhotoUrl(),
            user.getCoverPhotoAlt()
        );
    }

    public static UserFullStatsDto toFullStats(
        User user,
        long articleCount,
        long recipeCount,
        long followingCount,
        long followersCount
    ) {
        return new UserFullStatsDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getUsername(),
            user.getProfilePictureUrl(),
            user.getProfilePictureAlt(),
            user.getBio(),
            user.getCoverPhotoUrl(),
            user.getCoverPhotoAlt(),
            user.getDateOfBirth(),
            recipeCount,
            articleCount,
            followersCount,
            followingCount
        );
    }
}
