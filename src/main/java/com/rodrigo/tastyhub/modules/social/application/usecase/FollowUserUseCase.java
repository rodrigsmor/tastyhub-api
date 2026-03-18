package com.rodrigo.tastyhub.modules.social.application.usecase;

import com.rodrigo.tastyhub.modules.social.domain.service.FollowService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowUserUseCase {
    private final SecurityService securityService;
    private final UserService userService;
    private final FollowService followService;

    @Transactional
    public void execute(Long followingId) {
        User followerUser = securityService.getCurrentUser();

        User targetUser = userService.findByIdOrThrow(followingId);

        followService.follow(followerUser, targetUser);
    }
}
