package com.rodrigo.tastyhub.modules.user.application.mapper;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserFullStatsDto;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserProfileDto;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
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

    public static UserProfileDto toProfile(User user) {
        return new UserProfileDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getUsername(),
            storageService.generateImageUrl(user.getProfilePictureUrl()),
            user.getProfilePictureAlt(),
            user.getBio(),
            storageService.generateImageUrl(user.getCoverPhotoUrl()),
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
            storageService.generateImageUrl(user.getProfilePictureUrl()),
            user.getProfilePictureAlt(),
            user.getBio(),
            storageService.generateImageUrl(user.getCoverPhotoUrl()),
            user.getCoverPhotoAlt(),
            user.getDateOfBirth(),
            recipeCount,
            articleCount,
            followersCount,
            followingCount
        );
    }
}
