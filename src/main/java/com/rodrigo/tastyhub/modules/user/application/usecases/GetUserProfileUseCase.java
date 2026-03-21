package com.rodrigo.tastyhub.modules.user.application.usecases;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserFullStatsDto;
import com.rodrigo.tastyhub.modules.user.application.mapper.UserMapper;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.projections.UserProfileProjection;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetUserProfileUseCase {
    private final UserService userService;
    private final SecurityService securityService;

    public UserFullStatsDto execute(Long userId) {
        Optional<User> requesterUser = this.securityService.getCurrentUserOptional();
        UserProfileProjection profile = userService.findProfileById(
            userId,
            requesterUser.map(User::getId).orElse(null)
        );

        return UserMapper.toFullStats(profile);
    }
}
