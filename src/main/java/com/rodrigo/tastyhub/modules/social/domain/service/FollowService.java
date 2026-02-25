package com.rodrigo.tastyhub.modules.social.domain.service;

import com.rodrigo.tastyhub.modules.social.domain.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    public long getFollowersCount(Long userId) {
        return followRepository.countByIdFollowingId(userId);
    }

    public long getFollowingCount(Long userId) {
        return followRepository.countByIdFollowerId(userId);
    }
}
