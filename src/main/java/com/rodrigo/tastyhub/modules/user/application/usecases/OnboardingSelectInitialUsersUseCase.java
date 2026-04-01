package com.rodrigo.tastyhub.modules.user.application.usecases;

import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingConnectionsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.response.OnboardingProgressDto;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.service.OnboardingService;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingSelectInitialUsersUseCase {
    private final UserService userService;
    private final SecurityService securityService;
    private final OnboardingService onboardingService;

    @Transactional
    public OnboardingProgressDto execute(OnboardingConnectionsRequest request, boolean shouldSkip) {
        var user = securityService.getCurrentUser();

        List<User> usersToFollow = new ArrayList<>();

        if (request.hasUserIds()) {
            usersToFollow = userService.findAllByIds(request.userIds());
        }

        user = onboardingService.followInitialUsers(
            user.getId(),
            usersToFollow,
            shouldSkip
        );

        return new OnboardingProgressDto(
            user.getOnboardingStatus(),
            user.getOnboardingStatus().getNext(),
            user.isOnboardingFinished()
        );
    }
}
