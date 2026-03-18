package com.rodrigo.tastyhub.modules.social.application.usecase;

import com.rodrigo.tastyhub.modules.social.domain.service.FollowService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import org.springframework.transaction.annotation.Transactional;
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

        if (followerUser.getId().equals(followingId)) {
            throw new DomainException("You cannot follow yourself");
        }

        User targetUser = userService.findByIdOrThrow(followingId);

        followService.follow(followerUser, targetUser);
    }
}
