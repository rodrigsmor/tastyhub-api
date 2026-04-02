package com.rodrigo.tastyhub.modules.user.domain.service;

import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingConnectionsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingInterestsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.response.OnboardingProgressDto;
import com.rodrigo.tastyhub.modules.user.domain.model.OnboardingStatus;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.model.UserStatus;
import com.rodrigo.tastyhub.modules.user.domain.repository.UserRepository;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import com.rodrigo.tastyhub.shared.exception.UnauthorizedException;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    @DisplayName("Tests for Follow Initial Users Method (Step 3)")
    class FollowInitialUsersTests {

        @Test
        @DisplayName("1. Should follow users and complete onboarding successfully")
        void shouldFollowUsersAndCompleteOnboarding() {
            fakeUser.setFollowing(new HashSet<>());
            Set<Long> userIds = Set.of(10L, 20L);
            var request = new OnboardingConnectionsRequest(userIds);

            User target1 = new User(); target1.setId(10L);
            User target2 = new User(); target2.setId(20L);
            List<User> targetUsers = List.of(target1, target2);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.findAllById(userIds)).thenReturn(targetUsers);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            ResponseEntity<OnboardingProgressDto> response = onboardingService.followInitialUsers(request, false);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(OnboardingStatus.COMPLETED, fakeUser.getOnboardingStatus());
            assertTrue(fakeUser.getFollowing().stream()
                    .anyMatch(follow -> follow.getFollowing().equals(target1)),
                "User should be following target1");

            assertTrue(fakeUser.getFollowing().stream()
                        .anyMatch(follow -> follow.getFollowing().equals(target2)),
                "User should be following target2");
            verify(userRepository).save(fakeUser);
        }

        @Test
        @DisplayName("2. Should skip following logic but still complete onboarding")
        void shouldSkipAndCompleteOnboarding() {
            OnboardingConnectionsRequest request = new OnboardingConnectionsRequest(Set.of(10L));
            when(securityService.getCurrentUser()).thenReturn(fakeUser);

            when(userRepository.save(any(User.class))).thenReturn(fakeUser);

            onboardingService.followInitialUsers(request, true);

            verify(userRepository, never()).findAllById(anySet());

            verify(userRepository, times(1)).save(fakeUser);

            assertEquals(OnboardingStatus.COMPLETED, fakeUser.getOnboardingStatus());
        }

        @Test
        @DisplayName("3. Should not follow self if currentUser ID is in the list")
        void shouldNotFollowSelf() {
            fakeUser.setId(1L);
            fakeUser.setFollowing(new HashSet<>());
            Set<Long> userIds = Set.of(1L);
            var request = new OnboardingConnectionsRequest(userIds);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.findAllById(userIds)).thenReturn(List.of(fakeUser));
            when(userRepository.save(any(User.class))).thenReturn(fakeUser);

            onboardingService.followInitialUsers(request, false);

            assertTrue(fakeUser.getFollowing().isEmpty(), "User should not be following themselves");
            assertEquals(OnboardingStatus.COMPLETED, fakeUser.getOnboardingStatus());
        }

        @Test
        @DisplayName("4. Should finalize onboarding even if request has no userIds")
        void shouldCompleteEvenIfNoUsersSelected() {
            var request = new OnboardingConnectionsRequest(Set.of());
            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.save(any(User.class))).thenReturn(fakeUser);

            onboardingService.followInitialUsers(request, false);

            assertEquals(OnboardingStatus.COMPLETED, fakeUser.getOnboardingStatus());
            verify(userRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("5. Should handle empty list from repository gracefully")
        void shouldHandleEmptyRepositoryResult() {
            Set<Long> invalidIds = Set.of(999L);
            var request = new OnboardingConnectionsRequest(invalidIds);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.findAllById(invalidIds)).thenReturn(List.of());
            when(userRepository.save(any(User.class))).thenReturn(fakeUser);

            onboardingService.followInitialUsers(request, false);

            assertEquals(OnboardingStatus.COMPLETED, fakeUser.getOnboardingStatus());
            assertTrue(fakeUser.getFollowing().isEmpty());
        }
    }

    @Nested
    @DisplayName("Tests for Back To Previous Step Method")
    class BackStepTests {

        @Test
        @DisplayName("1. Should throw ForbiddenException if onboarding is already finished")
        void shouldThrowForbiddenIfCompleted() {
            fakeUser.setOnboardingStatus(OnboardingStatus.COMPLETED);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);

            assertThrows(ForbiddenException.class, () -> onboardingService.backToPreviousStep());
        }

        @Test
        @DisplayName("2. Should throw UnauthorizedException if account is not verified")
        void shouldThrowUnauthorizedIfNotVerified() {
            fakeUser.setStatus(UserStatus.PENDING);
            fakeUser.setOnboardingStatus(OnboardingStatus.PENDING_VERIFICATION);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);

            assertThrows(UnauthorizedException.class, () -> onboardingService.backToPreviousStep());
        }

        @Test
        @DisplayName("3. Should throw BadRequestException if already at STEP_1")
        void shouldThrowBadRequestIfAtStep1() {
            fakeUser.setOnboardingStatus(OnboardingStatus.STEP_1);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);

            BadRequestException ex = assertThrows(BadRequestException.class,
                () -> onboardingService.backToPreviousStep());

            assertTrue(ex.getMessage().contains("initial onboarding step"));
        }

        @Test
        @DisplayName("4. Should revert from STEP_3 to STEP_2 successfully")
        void shouldRevertFromStep3ToStep2() throws BadRequestException {
            fakeUser.setOnboardingStatus(OnboardingStatus.STEP_3);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            ResponseEntity<OnboardingProgressDto> response = onboardingService.backToPreviousStep();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(OnboardingStatus.STEP_2, fakeUser.getOnboardingStatus());
            verify(userRepository).save(fakeUser);
        }

        @Test
        @DisplayName("5. Should revert from STEP_2 to STEP_1 successfully")
        void shouldRevertFromStep2ToStep1() throws BadRequestException {
            fakeUser.setOnboardingStatus(OnboardingStatus.STEP_2);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            onboardingService.backToPreviousStep();

            assertEquals(OnboardingStatus.STEP_1, fakeUser.getOnboardingStatus());
            verify(userRepository).save(fakeUser);
        }
    }

    @Nested
    @DisplayName("Tests for Get Current Step Method")
    class GetCurrentStepTests {

        @Test
        @DisplayName("Should return progress details based on user status")
        void shouldReturnProgressDetails() {
            fakeUser.setOnboardingStatus(OnboardingStatus.STEP_2);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);

            OnboardingProgressDto result = onboardingService.getCurrentStep();

            assertNotNull(result);
            assertEquals(OnboardingStatus.STEP_2, result.currentStatus());
            assertEquals(OnboardingStatus.STEP_3, result.nextStepAction());
            assertFalse(result.isCompleted());
        }

        @Test
        @DisplayName("Should return isCompleted true when status is finished")
        void shouldReturnCompletedTrue() {
            fakeUser.setOnboardingStatus(OnboardingStatus.COMPLETED);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);

            OnboardingProgressDto result = onboardingService.getCurrentStep();

            assertTrue(result.isCompleted());
            assertEquals(OnboardingStatus.COMPLETED, result.currentStatus());
        }
    }
}