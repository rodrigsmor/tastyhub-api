package com.rodrigo.tastyhub.modules.user.domain.model;

import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.settings.domain.model.UserSettings;
import com.rodrigo.tastyhub.modules.social.domain.model.Follow;
import com.rodrigo.tastyhub.modules.social.domain.model.FollowId;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
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

    @Builder.Default
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recipe> recipes = new ArrayList<>();

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
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserSettings settings = new UserSettings();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, columnDefinition = "user_status_enum")
    private UserStatus status = UserStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status", length = 50, columnDefinition = "onboarding_status_enum")
    private OnboardingStatus onboardingStatus = OnboardingStatus.PENDING_VERIFICATION;

    @Column(name = "onboarding_started_at")
    private OffsetDateTime onboardingStartedAt;

    @Column(name = "onboarding_completed_at")
    private OffsetDateTime onboardingCompletedAt;

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
        return this.getStatus() == UserStatus.ACTIVE
            || this.getOnboardingStatus() != OnboardingStatus.PENDING_VERIFICATION;
    }

    public Boolean isOnboardingFinished() {
        return this.getOnboardingStatus() == OnboardingStatus.COMPLETED;
    }

    public void startOnboarding() {
        if (this.onboardingStatus != OnboardingStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Cannot start onboarding from current state");
        }
        this.onboardingStatus = OnboardingStatus.STEP_1;
        this.onboardingStartedAt = OffsetDateTime.now();
    }

    public void completeOnboarding() {
        this.onboardingStatus = OnboardingStatus.COMPLETED;
        this.status = UserStatus.ACTIVE;
        this.onboardingCompletedAt = OffsetDateTime.now();
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

    public void addRecipe(Recipe recipe) {
        this.recipes.add(recipe);
        recipe.setAuthor(this);
    }
}
