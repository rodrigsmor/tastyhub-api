package com.rodrigo.tastyhub.modules.user.domain.service;

import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingProfileRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.response.OnboardingProgressDto;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingConnectionsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingInterestsRequest;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.model.OnboardingStatus;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.repository.UserRepository;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
import com.rodrigo.tastyhub.shared.exception.UnauthorizedException;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.kernel.annotations.FileCleanup;
import com.rodrigo.tastyhub.shared.kernel.annotations.FileRollback;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class OnboardingService {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TagService tagService;

    @Transactional
    @FileRollback
    @FileCleanup
    public ResponseEntity<OnboardingProgressDto> updateUserProfile(OnboardingProfileRequest request, MultipartFile file) {
        User user = securityService.getCurrentUser();

        if (userRepository.existsByUsernameAndIdNot(request.username(), user.getId())) {
            throw new BadCredentialsException("Invalid input or username already taken");
        }

        if (file != null && !file.isEmpty()) {
            userService.updateProfilePicture(file, request.profilePictureAlt());
        }

        user.setBio(request.bio());
        user.setUsername(request.username());
        user.setDateOfBirth(request.dateOfBirth());
        user.setOnboardingStatus(OnboardingStatus.STEP_2);

        return ResponseEntity.ok(this.getOnboardingProgressResponse(userRepository.save(user)));
    }

    public void startOnboarding(User user) {
        user.startOnboarding();
        userRepository.save(user);
    }

    @Transactional
    public ResponseEntity<OnboardingProgressDto> selectInterests(
        OnboardingInterestsRequest request,
        boolean shouldSkip
    ) {
        User user = securityService.getCurrentUser();

        if (shouldSkip) {
            return completeStepAndResponse(user);
        }

        if (request.hasUnfollowTagIds()) {
            List<Tag> tagsToUnfollow = tagService.findAllById(request.unfollowTagIds());
            tagsToUnfollow.forEach(user::unfollowTag);
        }

        if (request.hasNewTags()) {
            Set<Tag> newTags = tagService.ensureTagsExist(request.newTags());
            newTags.forEach(user::followTag);
        }

        if (request.hasTagIds()) {
            List<Tag> existingTags = tagService.findAllById(request.tagIds());
            existingTags.forEach(user::followTag);
        }

        return completeStepAndResponse(user);
    }

    @Transactional
    public ResponseEntity<OnboardingProgressDto> followInitialUsers(OnboardingConnectionsRequest request, boolean shouldSkip) {
        User currentUser = securityService.getCurrentUser();

        if (shouldSkip) {
            return finalizeOnboarding(currentUser);
        }

        if (request.hasUserIds()) {
            List<User> usersToFollow = userRepository.findAllById(request.userIds());

            for (User targetUser : usersToFollow) {
                if (!targetUser.getId().equals(currentUser.getId())) {
                    currentUser.followUser(targetUser);
                }
            }
        }

        return finalizeOnboarding(currentUser);
    }

    @Transactional
    public ResponseEntity<OnboardingProgressDto> backToPreviousStep() throws BadRequestException {
        User user = securityService.getCurrentUser();

        if (user.isOnboardingFinished()) {
            throw new ForbiddenException("Onboarding has already been completed. Status cannot be reverted.");        }

        if (!user.isVerified()) {
            throw new UnauthorizedException("Account not verified. Please check your email to proceed.");
        }

        OnboardingStatus currentStatus = user.getOnboardingStatus();

        if (currentStatus == OnboardingStatus.STEP_1) {
            throw new BadRequestException("Cannot go back: User is already at the initial onboarding step.");
        }

        OnboardingStatus previousStep = (currentStatus == OnboardingStatus.STEP_3)
            ? OnboardingStatus.STEP_2
            : OnboardingStatus.STEP_1;

        user.setOnboardingStatus(previousStep);

        return ResponseEntity.ok(
                this.getOnboardingProgressResponse(userRepository.save(user)
            )
        );
    }

    public OnboardingProgressDto getCurrentStep() {
        User user = securityService.getCurrentUser();

        OnboardingStatus status = user.getOnboardingStatus();

        return new OnboardingProgressDto(
            status,
            status.getNext(),
            user.isOnboardingFinished()
        );
    }

    private ResponseEntity<OnboardingProgressDto> completeStepAndResponse(User user) {
        user.setOnboardingStatus(OnboardingStatus.STEP_3);

        return ResponseEntity.ok(
                this.getOnboardingProgressResponse(userRepository.save(user)
            )
        );
    }

    private ResponseEntity<OnboardingProgressDto> finalizeOnboarding(User user) {
        user.completeOnboarding();

        return ResponseEntity.ok(
                this.getOnboardingProgressResponse(userRepository.save(user)
            )
        );
    }

    private OnboardingProgressDto getOnboardingProgressResponse(User user) {
        return new OnboardingProgressDto(
            user.getOnboardingStatus(),
            user.getOnboardingStatus().getNext(),
            user.isOnboardingFinished()
        );
    }
}
