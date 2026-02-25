package com.rodrigo.tastyhub.modules.user.domain.service;

import com.rodrigo.tastyhub.modules.articles.domain.service.ArticleService;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.social.domain.service.FollowService;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserFullStatsDto;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import com.rodrigo.tastyhub.modules.user.application.mapper.UserMapper;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.repository.UserRepository;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import com.rodrigo.tastyhub.shared.kernel.annotations.FileCleanup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ArticleService articleService;

    private final RecipeService recipeService;

    private final FollowService followService;

    private final UserRepository userRepository;

    private final ImageStorageService imageStorageService;

    private final SecurityService securityService;

    public UserFullStatsDto getUserProfileById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long articleCount = articleService.getArticlesCountByUserId(userId);
        long recipeCount = recipeService.getRecipesCountByUserId(userId);
        long followersCount = followService.getFollowersCount(userId);
        long followingCount = followService.getFollowingCount(userId);

        return UserMapper.toFullStats(
            user,
            articleCount,
            recipeCount,
            followersCount,
            followingCount
        );
    }

    @FileCleanup
    public UserSummaryDto updateProfilePicture(MultipartFile file, String alternativeText) {
        User user = securityService.getCurrentUser();
        String oldFileName = user.getProfilePictureUrl();

        String filename = imageStorageService.storeImage(file);

        user.setProfilePictureUrl(filename);
        user.setProfilePictureAlt(alternativeText);

        userRepository.save(user);

        if (oldFileName != null) {
            imageStorageService.deleteImage(oldFileName);
        }

        return UserMapper.toSummary(user);
    }
}
