package com.rodrigo.tastyhub.modules.user.domain.service;

import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingConnectionsRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingIdentityRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingInterestsRequest;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.model.OnBoardingStatus;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.tags.domain.repository.TagRepository;
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

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    @Transactional
    @FileRollback
    @FileCleanup
    public ResponseEntity<Void> updateUserProfile(OnboardingIdentityRequest request, MultipartFile file) {
        User user = securityService.getCurrentUser();

        if (userRepository.existsByUsernameAndIdNot(request.username(), user.getId())) {
            throw new BadCredentialsException("Invalid input or username already taken");
        }

        if (file != null && !file.isEmpty()) {
            userService.updateProfilePicture(file, request.profilePictureAlt());
        }

        user.setBio(request.bio());
        user.setUsername(request.username());
        user.setOnBoardingStatus(OnBoardingStatus.STEP_2);

        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

    @Transactional
    public ResponseEntity<Void> selectInterests(
        OnboardingInterestsRequest request,
        boolean shouldSkip
    ) {
        User user = securityService.getCurrentUser();

        if (shouldSkip) {
            return completeStepAndResponse(user);
        }

        if (request.hasUnfollowTagIds()) {
            List<Tag> tagsToUnfollow = tagRepository.findAllById(request.unfollowTagIds());
            tagsToUnfollow.forEach(user::unfollowTag);
        }

        if (request.hasNewTags()) {
            Set<Tag> newTags = tagService.ensureTagsExist(request.newTags());
            newTags.forEach(user::followTag);
        }

        if (request.hasTagIds()) {
            List<Tag> existingTags = tagRepository.findAllById(request.tagIds());
            existingTags.forEach(user::followTag);
        }

        return completeStepAndResponse(user);
    }

    @Transactional
    public ResponseEntity<Void> followInitialUsers(OnboardingConnectionsRequest request, boolean shouldSkip) {
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
    public ResponseEntity<Void> backToPreviousStep() throws BadRequestException {
        User user = securityService.getCurrentUser();

        if (user.isOnboardingFinished()) {
            throw new ForbiddenException("Onboarding has already been completed. Status cannot be reverted.");        }

        if (!user.isVerified()) {
            throw new UnauthorizedException("Account not verified. Please check your email to proceed.");
        }

        OnBoardingStatus currentStatus = user.getOnBoardingStatus();

        if (currentStatus == OnBoardingStatus.STEP_1) {
            throw new BadRequestException("Cannot go back: User is already at the initial onboarding step.");
        }

        OnBoardingStatus previousStep = (currentStatus == OnBoardingStatus.STEP_3)
            ? OnBoardingStatus.STEP_2
            : OnBoardingStatus.STEP_1;

        user.setOnBoardingStatus(previousStep);

        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Void> completeStepAndResponse(User user) {
        user.setOnBoardingStatus(OnBoardingStatus.STEP_3);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Void> finalizeOnboarding(User user) {
        user.completeOnboarding();
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }
}
