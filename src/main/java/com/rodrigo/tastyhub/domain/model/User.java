package com.rodrigo.tastyhub.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName;

    @Column(nullable = false, unique = true, length = 92)
    private String email;

    @Column(unique = true, length = 50)
    private String phone;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(length = 280)
    private String bio;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String password;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_following_tags",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> followedTags = new HashSet<>();

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_picture_url", columnDefinition = "TEXT")
    private String profilePictureUrl;

    @Column(name = "profile_picture_alt")
    private String profilePictureAlt;

    @Column(name = "cover_photo_url", columnDefinition = "TEXT")
    private String coverPhotoUrl;

    @Column(name = "cover_photo_alt")
    private String coverPhotoAlt;

    @Builder.Default
    @OneToMany(
        mappedBy = "follower",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private Set<Follow> following = new HashSet<>();

    @Builder.Default
    @OneToMany(
        mappedBy = "following",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private Set<Follow> followers = new HashSet<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private UserStatus status = UserStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "on_boarding_status", length = 50)
    private OnBoardingStatus onBoardingStatus = OnBoardingStatus.PENDING_VERIFICATION;

    @Column(name = "on_boarding_started_at")
    private OffsetDateTime onBoardingStartedAt;

    @Column(name = "on_boarding_completed_at")
    private OffsetDateTime onBoardingCompletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getName().authority()))
            .collect(Collectors.toList());
    }

    public Boolean isVerified() {
        return this.getStatus() != UserStatus.PENDING
            && this.getOnBoardingStatus() != OnBoardingStatus.PENDING_VERIFICATION;
    }

    public Boolean isOnboardingFinished() {
        return this.getOnBoardingStatus() != OnBoardingStatus.COMPLETED;
    }

    public void startOnboarding() {
        if (this.onBoardingStatus != OnBoardingStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Cannot start onboarding from current state");
        }
        this.onBoardingStatus = OnBoardingStatus.STEP_1;
        this.onBoardingStartedAt = OffsetDateTime.now();
    }

    public void completeOnboarding() {
        this.onBoardingStatus = OnBoardingStatus.COMPLETED;
        this.status = UserStatus.ACTIVE;
        this.onBoardingCompletedAt = OffsetDateTime.now();
    }

    public void followTag(Tag tag) {
        this.followedTags.add(tag);
        tag.getFollowers().add(this);
    }

    public void unfollowTag(Tag tag) {
        this.followedTags.remove(tag);
        tag.getFollowers().remove(this);
    }

    public void followUser(User targetUser) {
        Follow follow = new Follow();
        follow.setId(new FollowId(this.id, targetUser.getId()));
        follow.setFollower(this);
        follow.setFollowing(targetUser);
        this.following.add(follow);
    }

    public void unfollowUser(User targetUser) {
        this.following.removeIf(follow ->
            follow.getFollowing().getId().equals(targetUser.getId())
        );

        targetUser.getFollowers().removeIf(follow ->
            follow.getFollower().getId().equals(this.id)
        );
    }
}
