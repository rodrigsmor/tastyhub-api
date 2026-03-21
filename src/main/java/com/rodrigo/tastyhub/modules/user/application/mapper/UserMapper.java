package com.rodrigo.tastyhub.modules.user.application.mapper;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserFullStatsDto;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserProfileDto;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.projections.UserProfileProjection;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class UserMapper {
    private static ImageStorageService storageService;

    @Autowired
    public void setStorageService(ImageStorageService storageService) {
        UserMapper.storageService = storageService;
    }

    public static UserSummaryDto toSummary(User user) {
        return new UserSummaryDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getUsername(),
            storageService.generateImageUrl(user.getProfilePictureUrl()),
            user.getProfilePictureAlt()
        );
    }

    public static UserProfileDto toProfile(UserProfileProjection user) {
        return new UserProfileDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getUsername(),
            storageService.generateImageUrl(user.getProfilePictureUrl()),
            user.getProfilePictureAlt(),
            user.getBio(),
            storageService.generateImageUrl(user.getCoverUrl()),
            user.getCoverAlt()
        );
    }

    public static UserFullStatsDto toFullStats(UserProfileProjection profile) {
        return new UserFullStatsDto(
            profile.getId(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getUsername(),
            profile.getVisibility(),
            storageService.generateImageUrl(profile.getProfilePictureUrl()),
            profile.getProfilePictureAlt(),
            profile.getBio(),
            storageService.generateImageUrl(profile.getCoverUrl()),
            profile.getCoverAlt(),
            profile.getDateOfBirth(),
            profile.getIsFollowing(),
            profile.getIsFollower(),
            profile.getRecipeCount() != null ? profile.getRecipeCount() : 0,
            profile.getArticleCount() != null ? profile.getArticleCount() : 0,
            profile.getFollowerCount() != null ? profile.getFollowerCount() : 0,
            profile.getFollowingCount() != null ? profile.getFollowingCount() : 0
        );
    }
}
