package com.rodrigo.tastyhub.shared.config.security;

import com.rodrigo.tastyhub.modules.social.domain.service.FollowService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurityEvaluator {
    private final FollowService followService;
    private final UserService userService;
    private final SecurityService securityService;

    public boolean canAccessProfile(Long targetUserId) {
        User targetUser = userService.findByIdOrThrow(targetUserId);

        if (!targetUser.isPrivate()) {
            return true;
        }

        Long requesterId = securityService.getCurrentUserOptional()
            .map(User::getId)
            .orElse(null);

        if (requesterId == null) {
            return false;
        }

        return targetUserId.equals(requesterId) || followService.isFollowing(requesterId, targetUserId);
    }
}
