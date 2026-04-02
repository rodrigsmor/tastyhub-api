package com.rodrigo.tastyhub.modules.user.domain.service;

import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.model.OnboardingStatus;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.model.UserStatus;
import com.rodrigo.tastyhub.modules.user.domain.repository.UserRepository;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import com.rodrigo.tastyhub.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private UserService userService;

    @InjectMocks
    private OnboardingService onboardingService;

    @Mock
    private TagService tagService;

    private User fakeUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setUsername("old.username");
        fakeUser.setBio("Old bio");

        fakeUser.setFollowedTags(new HashSet<>());
        fakeUser.setOnboardingStatus(OnboardingStatus.STEP_1);
    }

    @Nested
    @DisplayName("Tests for Find User By Id")
    class FindByIdTests {
        @Test
        @DisplayName("Should return user when a valid ID is provided")
        void shouldReturnUserWhenIdExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));

            User result = onboardingService.findUserById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("old.username", result.getUsername());
            verify(userRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user does not exist")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                onboardingService.findUserById(999L)
            );

            assertEquals("The user provided cannot be found", exception.getMessage());
            verify(userRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Should call repository with correct ID")
        void shouldCallRepositoryWithCorrectParameters() {
            Long targetId = 5L;
            fakeUser.setId(targetId);
            when(userRepository.findById(targetId)).thenReturn(Optional.of(fakeUser));

            onboardingService.findUserById(targetId);

            verify(userRepository).findById(eq(targetId));
        }
    }

    @Nested
    @DisplayName("Tests for Update Profile")
    class UpdateProfileTests {
        @Test
        @DisplayName("Should successfully update user profile and advance to STEP_2")
        void shouldUpdateProfileSuccessfully() {
            Long userId = 1L;
            String newUsername = "chef.tasty";
            String newBio = "Passionate about spices.";
            String newPic = "https://cdn.com/avatar.png";
            String newAlt = "Profile of Chef Tasty";
            LocalDate dob = LocalDate.of(1990, 5, 15);

            when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));
            when(userRepository.existsByUsernameAndIdNot(newUsername, userId)).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = onboardingService.updateProfile(
                userId,
                newUsername,
                newBio,
                newPic,
                newAlt,
                dob
            );

            assertAll(
                () -> assertEquals(newUsername, result.getUsername()),
                () -> assertEquals(newBio, result.getBio()),
                () -> assertEquals(newPic, result.getProfilePictureUrl()),
                () -> assertEquals(newAlt, result.getProfilePictureAlt()),
                () -> assertEquals(dob, result.getDateOfBirth()),
                () -> assertEquals(OnboardingStatus.STEP_2, result.getOnboardingStatus())
            );
            verify(userRepository).save(fakeUser);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when username is already taken")
        void shouldThrowExceptionWhenUsernameTaken() {
            Long userId = 1L;
            String takenUsername = "already.exists";

            when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));
            when(userRepository.existsByUsernameAndIdNot(takenUsername, userId)).thenReturn(true);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                onboardingService.updateProfile(
                    userId,
                    takenUsername,
                    "bio",
                    null,
                    null,
                    LocalDate.now()
                )
            );

            assertEquals("Invalid input or username already taken", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should not overwrite profile picture if profileUrl is null")
        void shouldKeepOldProfilePictureIfUrlIsNull() {
            fakeUser.setProfilePictureUrl("old-image.jpg");
            fakeUser.setProfilePictureAlt("Old alt");

            when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = onboardingService.updateProfile(
                1L,
                "new.user",
                "bio",
                null,
                "new alt",
                LocalDate.now()
            );

            assertEquals("old-image.jpg", result.getProfilePictureUrl());
            assertEquals("Old alt", result.getProfilePictureAlt());
        }
    }

    @Nested
    @DisplayName("Tests for Select Interests")
    class SelectInterestsTests {
        @Test
        @DisplayName("Should update interests and advance to STEP_3 when not skipping")
        void shouldUpdateInterestsAndAdvanceToStep3() {
            Long userId = 1L;
            List<Tag> tagsToFollow = List.of(new Tag(1L, "Italian"), new Tag(2L, "Pasta"));
            List<Tag> tagsToUnfollow = List.of(new Tag(3L, "Fast Food"));

            when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = onboardingService.selectInterests(
                userId,
                tagsToFollow,
                tagsToUnfollow,
                false
            );

            assertEquals(OnboardingStatus.STEP_3, result.getOnboardingStatus());
            verify(userRepository).save(fakeUser);
        }

        @Test
        @DisplayName("Should skip interest update but still advance to STEP_3 when shouldSkip is true")
        void shouldSkipInterestsButAdvanceStatus() {
            Long userId = 1L;
            List<Tag> tags = List.of(new Tag(1L, "Gourmet"));

            User spyUser = spy(fakeUser);
            when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = onboardingService.selectInterests(
                userId,
                tags,
                null,
                true
            );

            assertEquals(OnboardingStatus.STEP_3, result.getOnboardingStatus());
            verify(spyUser, never()).updateInterests(any(), any());
            verify(userRepository).save(spyUser);
        }

        @Test
        @DisplayName("Should work correctly even if tag collections are null")
        void shouldHandleNullCollectionsGracefully() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            assertDoesNotThrow(() ->
                onboardingService.selectInterests(
                    1L,
                    null,
                    null,
                    false
                )
            );

            assertEquals(OnboardingStatus.STEP_3, fakeUser.getOnboardingStatus());
        }
    }

    @Nested
    @DisplayName("Tests for Follow Initial Users")
    class FollowInitialUsersTests {
        @Test
        @DisplayName("Should follow users and complete onboarding when not skipping")
        void shouldFollowUsersAndCompleteOnboarding() {
            Long userId = 1L;
            User userToFollow = new User();
            userToFollow.setId(2L);
            Collection<User> usersToFollow = List.of(userToFollow);

            User spyUser = spy(fakeUser);
            when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = onboardingService.followInitialUsers(
                userId,
                usersToFollow,
                false
            );

            verify(spyUser).followUser(userToFollow);
            verify(spyUser).completeOnboarding();
            assertEquals(OnboardingStatus.COMPLETED, result.getOnboardingStatus());
            verify(userRepository).save(spyUser);
        }

        @Test
        @DisplayName("Should not follow anyone if user attempts to follow themselves")
        void shouldNotFollowSelf() {
            Long userId = 1L;
            fakeUser.setId(userId);
            Collection<User> usersToFollow = List.of(fakeUser);

            User spyUser = spy(fakeUser);
            when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            onboardingService.followInitialUsers(
                userId,
                usersToFollow,
                false
            );

            verify(spyUser, never()).followUser(any());
            verify(spyUser).completeOnboarding();
        }

        @Test
        @DisplayName("Should skip following but still complete onboarding when shouldSkip is true")
        void shouldSkipFollowingButStillComplete() {
            Long userId = 1L;
            User anotherUser = new User();
            anotherUser.setId(2L);
            Collection<User> usersToFollow = List.of(anotherUser);

            User spyUser = spy(fakeUser);
            when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = onboardingService.followInitialUsers(
                userId,
                usersToFollow,
                true
            );

            verify(spyUser, never()).followUser(any());
            verify(spyUser).completeOnboarding();
            assertEquals(OnboardingStatus.COMPLETED, result.getOnboardingStatus());
        }
    }

    @Nested
    @DisplayName("Tests for Back to Previous Step")
    class BackToPreviousStepTests {
        @Test
        @DisplayName("Should revert from STEP_3 to STEP_2 successfully")
        void shouldRevertFromStep3ToStep2() {
            fakeUser.setOnboardingStatus(OnboardingStatus.STEP_3);
            fakeUser.setStatus(UserStatus.ACTIVE);
            fakeUser.setOnboardingStatus(OnboardingStatus.STEP_2);

            when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            User result = onboardingService.backToPreviousStep(1L);

            assertEquals(OnboardingStatus.STEP_2, result.getOnboardingStatus());
            verify(userRepository).save(fakeUser);
        }

        @Test
        @DisplayName("Should throw ForbiddenException if onboarding is already COMPLETED")
        void shouldThrowForbiddenIfCompleted() {
            fakeUser.setOnboardingStatus(OnboardingStatus.COMPLETED);
            when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));

            assertThrows(ForbiddenException.class, () ->
                onboardingService.backToPreviousStep(1L)
            );
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException if user is not verified")
        void shouldThrowUnauthorizedIfNotVerified() {
            fakeUser.setOnboardingStatus(OnboardingStatus.STEP_2);
            fakeUser.setStatus(UserStatus.PENDING);

            when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));

            assertThrows(UnauthorizedException.class, () ->
                onboardingService.backToPreviousStep(1L)
            );
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if already at STEP_1")
        void shouldThrowErrorIfAtFirstStep() {
            fakeUser.setOnboardingStatus(OnboardingStatus.STEP_1);
            fakeUser.setStatus(UserStatus.ACTIVE);

            when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                onboardingService.backToPreviousStep(1L)
            );

            assertEquals("Cannot go back: User is already at the initial onboarding step.", exception.getMessage());
        }
    }
}