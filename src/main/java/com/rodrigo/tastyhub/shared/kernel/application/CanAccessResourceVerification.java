package com.rodrigo.tastyhub.shared.kernel.application;

import com.rodrigo.tastyhub.modules.social.domain.service.FollowService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CanAccessResourceVerification {
    private final FollowService followService;

    public void verify(User owner, Long requesterId) {
        if (owner.isPrivate()) {
            if (requesterId == null) {
                throw new UnauthorizedException("This is a private resource. Please, authenticate to access.");
            }

            if (
                !owner.getId().equals(requesterId)
                && !followService.isFollowing(
                    requesterId,
                    owner.getId()
                )
            ) {
                throw new UnauthorizedException("You can not access this resource!");
            }
        }
    }
}
