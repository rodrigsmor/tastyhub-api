package com.rodrigo.tastyhub.modules.social.domain.service;

import com.rodrigo.tastyhub.modules.social.domain.model.Follow;
import com.rodrigo.tastyhub.modules.social.domain.model.FollowId;
import com.rodrigo.tastyhub.modules.social.domain.repository.FollowRepository;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByIdFollowerIdAndIdFollowingId(followerId, followingId);
    }

    public long getFollowersCount(Long userId) {
        return followRepository.countByIdFollowingId(userId);
    }

    public long getFollowingCount(Long userId) {
        return followRepository.countByIdFollowerId(userId);
    }

    public void follow(User follower, User following) {
        FollowId followId = new FollowId(follower.getId(), following.getId());

        if (followRepository.existsById(followId)) {
            throw new DomainException("You are already following this user");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        follow.setId(followId);

        followRepository.save(follow);
    }

    public void unfollow(User follower, User following) {
        FollowId followId = new FollowId(follower.getId(), following.getId());

        if (!followRepository.existsById(followId)) {
            throw new ResourceNotFoundException("You are not following this user");
        }

        followRepository.deleteById(followId);
    }
}
