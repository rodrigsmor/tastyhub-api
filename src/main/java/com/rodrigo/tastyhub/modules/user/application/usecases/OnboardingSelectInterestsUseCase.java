package com.rodrigo.tastyhub.modules.user.application.usecases;

import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingInterestsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.response.OnboardingProgressDto;
import com.rodrigo.tastyhub.modules.user.domain.service.OnboardingService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingSelectInterestsUseCase {
    private final SecurityService securityService;
    private final OnboardingService onboardingService;
    private final TagService tagService;

    @Transactional
    public OnboardingProgressDto execute(OnboardingInterestsRequest request, Boolean shouldSkip) {
        var user = securityService.getCurrentUser();

        List<Tag> tagsToFollow = new ArrayList<>();
        List<Tag> unfollowTags = new ArrayList<>();

        if (request.hasUnfollowTagIds()) {
            unfollowTags.addAll(
                tagService.findAllById(request.unfollowTagIds())
            );
        }

        if (request.hasNewTags()) {
            tagsToFollow.addAll(
                tagService.ensureTagsExist(request.newTags())
            );
        }

        if (request.hasTagIds()) {
            tagsToFollow.addAll(
                tagService.findAllById(request.tagIds())
            );
        }

        user = onboardingService.selectInterests(
            user.getId(),
            tagsToFollow,
            unfollowTags,
            shouldSkip
        );

        return new OnboardingProgressDto(
            user.getOnboardingStatus(),
            user.getOnboardingStatus().getNext(),
            user.isOnboardingFinished()
        );
    }
}
