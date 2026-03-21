package com.rodrigo.tastyhub.modules.auth.application.usecases;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserFullStatsDto;
import com.rodrigo.tastyhub.modules.user.application.mapper.UserMapper;
import com.rodrigo.tastyhub.modules.user.domain.projections.UserProfileProjection;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMyProfileUseCase {
    private final UserService userService;
    private final SecurityService securityService;

    public UserFullStatsDto execute() {
        Long userId = this.securityService.getCurrentUser().getId();
        UserProfileProjection profile = userService.findProfileById(userId, null);

        return UserMapper.toFullStats(profile);
    }
}
