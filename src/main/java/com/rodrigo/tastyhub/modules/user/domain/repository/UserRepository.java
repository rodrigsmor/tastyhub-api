package com.rodrigo.tastyhub.modules.user.domain.repository;

import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.projections.UserProfileProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
    Boolean existsByIdIn(Collection<Long> id);

    @Query("""
        SELECT
            u.id AS id,
            u.firstName AS firstName,
            u.lastName AS lastName,
            u.username AS username,
            u.profilePictureUrl AS profilePictureUrl,
            u.profilePictureAlt AS profilePictureAlt,
            u.bio AS bio,
            u.coverPhotoUrl AS coverUrl,
            u.coverPhotoAlt AS coverAlt,
            u.settings.profileVisibility AS visibility,
            u.dateOfBirth AS dateOfBirth,
            (SELECT COUNT(r) FROM Recipe r WHERE r.author.id = u.id) AS recipeCount,
            (SELECT COUNT(r) FROM Recipe r WHERE r.author.id = u.id) AS articleCount,
            (SELECT COUNT(f.id.followerId) FROM Follow f WHERE f.id.followingId = u.id) AS followerCount,
            (SELECT COUNT(f.id.followingId) FROM Follow f WHERE f.id.followerId = u.id) AS followingCount,
            (SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
             FROM Follow f WHERE f.id.followerId = :currentUserId AND f.id.followingId = u.id) AS isFollowing,
            (SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
             FROM Follow f WHERE f.id.followerId = u.id AND f.id.followingId = :currentUserId) AS isFollower
        FROM User u
        WHERE u.id = :userId
    """)
    Optional<UserProfileProjection> findProfileById(
        @Param("userId") Long userId,
        @Param("currentUserId") Long currentUserId
    );
}
