package com.rodrigo.tastyhub.modules.user.application.usecases;

import com.rodrigo.tastyhub.modules.user.application.dto.request.OnboardingProfileRequest;
import com.rodrigo.tastyhub.modules.user.application.dto.response.OnboardingProgressDto;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.service.OnboardingService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.shared.kernel.annotations.FileCleanup;
import com.rodrigo.tastyhub.shared.kernel.annotations.FileRollback;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OnboardingUpdateProfileUseCase {
    private final SecurityService securityService;
    private final OnboardingService onboardingService;
    private final ImageStorageService imageStorageService;

    @Transactional
    @FileRollback
    @FileCleanup
    public OnboardingProgressDto execute(OnboardingProfileRequest request, MultipartFile file) {
        User user = securityService.getCurrentUser();
        String oldFileName = user.getProfilePictureUrl();

        String filenameUrl = null;

        if (file != null && !file.isEmpty()) {
            filenameUrl = imageStorageService.storeImage(file);
        }

        User updateUser = onboardingService.updateProfile(
            user.getId(),
            request.username(),
            request.bio(),
            filenameUrl,
            request.profilePictureAlt(),
            request.dateOfBirth()
        );

        if (oldFileName != null) {
            imageStorageService.deleteImage(oldFileName);
        }

        return new OnboardingProgressDto(
            updateUser.getOnboardingStatus(),
            updateUser.getOnboardingStatus().getNext(),
            updateUser.isOnboardingFinished()
        );
    }
}
