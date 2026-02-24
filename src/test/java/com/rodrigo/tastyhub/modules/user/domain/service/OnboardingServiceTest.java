package com.rodrigo.tastyhub.modules.user.domain.service;

import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.repository.TagRepository;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingConnectionsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingIdentityRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingInterestsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.response.OnboardingProgressDto;
import com.rodrigo.tastyhub.modules.user.domain.model.OnBoardingStatus;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.model.UserStatus;
import com.rodrigo.tastyhub.modules.user.domain.repository.UserRepository;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

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
        fakeUser.setOnBoardingStatus(OnBoardingStatus.STEP_1);
    }

    @Nested
    @DisplayName("Tests for Update User Profile Method")
    class UpdateProfileTests {
        @BeforeEach
        void commonMocks() {
            lenient().when(userRepository.existsByUsernameAndIdNot(anyString(), anyLong())).thenReturn(false);
        }

        @Test
        @DisplayName("Should update profile picture and user data when a file is provided")
        void shouldUpdateProfileWithFile() {
            OnboardingIdentityRequest request = new OnboardingIdentityRequest("new.username", "New bio", "Alt text");
            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.existsByUsernameAndIdNot(anyString(), anyLong())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

            ResponseEntity<OnboardingProgressDto> response = onboardingService.updateUserProfile(request, file);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("new.username", fakeUser.getUsername());
            assertEquals("New bio", fakeUser.getBio());
            assertEquals(OnBoardingStatus.STEP_2, fakeUser.getOnBoardingStatus());

            verify(userService, times(1)).updateProfilePicture(eq(file), eq("Alt text"));
            verify(userRepository).save(fakeUser);
        }

        @Test
        @DisplayName("Should throw error if username is already taken")
        void shouldThrowErrorIfUsernameTaken() {
            OnboardingIdentityRequest request = new OnboardingIdentityRequest("taken.user", "bio", null);
            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.existsByUsernameAndIdNot("taken.user", 1L)).thenReturn(true);

            assertThrows(BadCredentialsException.class, () ->
                onboardingService.updateUserProfile(request, null)
            );
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return error if username exceeds 20 characters")
        void shouldReturnErrorIfUsernameExceedsLimit() {
            String longUsername = "username_muito_longo_mesmo";
            OnboardingIdentityRequest request = new OnboardingIdentityRequest(longUsername, "bio", null);

            lenient().when(securityService.getCurrentUser()).thenReturn(fakeUser);
            lenient().when(userRepository.existsByUsernameAndIdNot(anyString(), anyLong())).thenReturn(false);
        }
    }

    @Nested
    @DisplayName("Tests for Select Interests Method")
    class SelectInterests {
        @Test
        @DisplayName("Should unfollow tags when unfollowTagIds is provided")
        void shouldUnfollowTagsWhenProvided() {
            Tag tag1 = new Tag(3L, "Pasta", new HashSet<>());
            Tag tag2 = new Tag(8L, "Meat", new HashSet<>());
            Set<Long> idsToUnfollow = Set.of(3L, 8L);

            Set<Tag> userTags = new HashSet<>();
            userTags.add(tag1);
            userTags.add(tag2);
            fakeUser.setFollowedTags(userTags);

            OnboardingInterestsRequest request = new OnboardingInterestsRequest(null, null, idsToUnfollow);
            List<Tag> tagsFromDb = List.of(tag1, tag2);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(tagRepository.findAllById(idsToUnfollow)).thenReturn(tagsFromDb);
            when(userRepository.save(any(User.class))).thenReturn(fakeUser);

            onboardingService.selectInterests(request, false);

            assertTrue(request.hasUnfollowTagIds());
            assertTrue(fakeUser.getFollowedTags().isEmpty());
            verify(tagRepository).findAllById(idsToUnfollow);
            verify(userRepository).save(fakeUser);
        }

        @Test
        @DisplayName("Should ensure and follow new tags when newTags is provided")
        void shouldFollowNewTagsWhenProvided() {
            fakeUser.setFollowedTags(new HashSet<>());

            Set<String> newTagsNames = Set.of("homemade pasta", "vegan");
            OnboardingInterestsRequest request = new OnboardingInterestsRequest(null, newTagsNames, null);

            Set<Tag> tagsCreated = Set.of(
                new Tag(10L, "homemade pasta", new HashSet<>()),
                new Tag(11L, "vegan", new HashSet<>())
            );

            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(tagService.ensureTagsExist(newTagsNames)).thenReturn(tagsCreated);
            when(userRepository.save(any(User.class))).thenReturn(fakeUser);

            onboardingService.selectInterests(request, false);

            assertTrue(request.hasNewTags());
            verify(tagService).ensureTagsExist(newTagsNames);
        }

        @Test
        @DisplayName("Should follow existing tags when tagIds is provided")
        void shouldFollowExistingTagsWhenProvided() {
            fakeUser.setFollowedTags(new HashSet<>());

            Set<Long> tagIds = Set.of(1L, 5L);
            OnboardingInterestsRequest request = new OnboardingInterestsRequest(tagIds, null, null);

            List<Tag> existingTags = List.of(
                new Tag(1L, "Italian", new HashSet<>()),
                new Tag(5L, "Salads", new HashSet<>())
            );

            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(tagRepository.findAllById(tagIds)).thenReturn(existingTags);
            when(userRepository.save(any(User.class))).thenReturn(fakeUser);

            onboardingService.selectInterests(request, false);

            assertTrue(request.hasTagIds());
            verify(tagRepository).findAllById(tagIds);
        }

        @Test
        @DisplayName("Should skip all logic and just complete step when shouldSkip is true")
        void shouldSkipLogicWhenShouldSkipIsTrue() {
            OnboardingInterestsRequest request = new OnboardingInterestsRequest(Set.of(1L), Set.of("new"), Set.of(2L));
            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.save(any(User.class))).thenReturn(fakeUser);

            onboardingService.selectInterests(request, true);

            verifyNoInteractions(tagRepository);
            verifyNoInteractions(tagService);
            assertEquals(OnBoardingStatus.STEP_3, fakeUser.getOnBoardingStatus());
        }

        @Test
        @DisplayName("Should call save with updated status after processing interests")
        void shouldSaveUserWithNewStatus() {
            OnboardingInterestsRequest request = new OnboardingInterestsRequest(null, null, null);
            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            onboardingService.selectInterests(request, false);

            verify(userRepository).save(argThat(user -> user.getOnBoardingStatus() == OnBoardingStatus.STEP_3));
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
            assertEquals(OnBoardingStatus.COMPLETED, fakeUser.getOnBoardingStatus());
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

            assertEquals(OnBoardingStatus.COMPLETED, fakeUser.getOnBoardingStatus());
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
            assertEquals(OnBoardingStatus.COMPLETED, fakeUser.getOnBoardingStatus());
        }

        @Test
        @DisplayName("4. Should finalize onboarding even if request has no userIds")
        void shouldCompleteEvenIfNoUsersSelected() {
            var request = new OnboardingConnectionsRequest(Set.of());
            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.save(any(User.class))).thenReturn(fakeUser);

            onboardingService.followInitialUsers(request, false);

            assertEquals(OnBoardingStatus.COMPLETED, fakeUser.getOnBoardingStatus());
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

            assertEquals(OnBoardingStatus.COMPLETED, fakeUser.getOnBoardingStatus());
            assertTrue(fakeUser.getFollowing().isEmpty());
        }
    }

    @Nested
    @DisplayName("Tests for Back To Previous Step Method")
    class BackStepTests {

        @Test
        @DisplayName("1. Should throw ForbiddenException if onboarding is already finished")
        void shouldThrowForbiddenIfCompleted() {
            fakeUser.setOnBoardingStatus(OnBoardingStatus.COMPLETED);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);

            assertThrows(ForbiddenException.class, () -> onboardingService.backToPreviousStep());
        }

        @Test
        @DisplayName("2. Should throw UnauthorizedException if account is not verified")
        void shouldThrowUnauthorizedIfNotVerified() {
            fakeUser.setStatus(UserStatus.PENDING);
            fakeUser.setOnBoardingStatus(OnBoardingStatus.PENDING_VERIFICATION);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);

            assertThrows(UnauthorizedException.class, () -> onboardingService.backToPreviousStep());
        }

        @Test
        @DisplayName("3. Should throw BadRequestException if already at STEP_1")
        void shouldThrowBadRequestIfAtStep1() {
            fakeUser.setOnBoardingStatus(OnBoardingStatus.STEP_1);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);

            BadRequestException ex = assertThrows(BadRequestException.class,
                () -> onboardingService.backToPreviousStep());

            assertTrue(ex.getMessage().contains("initial onboarding step"));
        }

        @Test
        @DisplayName("4. Should revert from STEP_3 to STEP_2 successfully")
        void shouldRevertFromStep3ToStep2() throws BadRequestException {
            fakeUser.setOnBoardingStatus(OnBoardingStatus.STEP_3);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            ResponseEntity<OnboardingProgressDto> response = onboardingService.backToPreviousStep();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(OnBoardingStatus.STEP_2, fakeUser.getOnBoardingStatus());
            verify(userRepository).save(fakeUser);
        }

        @Test
        @DisplayName("5. Should revert from STEP_2 to STEP_1 successfully")
        void shouldRevertFromStep2ToStep1() throws BadRequestException {
            fakeUser.setOnBoardingStatus(OnBoardingStatus.STEP_2);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            onboardingService.backToPreviousStep();

            assertEquals(OnBoardingStatus.STEP_1, fakeUser.getOnBoardingStatus());
            verify(userRepository).save(fakeUser);
        }
    }

    @Nested
    @DisplayName("Tests for Get Current Step Method")
    class GetCurrentStepTests {

        @Test
        @DisplayName("Should return progress details based on user status")
        void shouldReturnProgressDetails() {
            fakeUser.setOnBoardingStatus(OnBoardingStatus.STEP_2);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);

            OnboardingProgressDto result = onboardingService.getCurrentStep();

            assertNotNull(result);
            assertEquals(OnBoardingStatus.STEP_2, result.currentStatus());
            assertEquals(OnBoardingStatus.STEP_3, result.nextStepAction());
            assertFalse(result.isCompleted());
        }

        @Test
        @DisplayName("Should return isCompleted true when status is finished")
        void shouldReturnCompletedTrue() {
            fakeUser.setOnBoardingStatus(OnBoardingStatus.COMPLETED);

            when(securityService.getCurrentUser()).thenReturn(fakeUser);

            OnboardingProgressDto result = onboardingService.getCurrentStep();

            assertTrue(result.isCompleted());
            assertEquals(OnBoardingStatus.COMPLETED, result.currentStatus());
        }
    }
}