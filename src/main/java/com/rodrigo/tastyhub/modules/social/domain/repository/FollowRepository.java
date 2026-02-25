package com.rodrigo.tastyhub.modules.social.domain.repository;

import com.rodrigo.tastyhub.modules.social.domain.model.Follow;
import com.rodrigo.tastyhub.modules.social.domain.model.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    long countByIdFollowingId(Long followingId);
    long countByIdFollowerId(Long followerId);
}
