package com.rodrigo.tastyhub.modules.user.domain.service;

import com.rodrigo.tastyhub.modules.auth.application.dto.request.SignupRequestDto;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import com.rodrigo.tastyhub.modules.user.application.mapper.UserMapper;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.projections.UserProfileProjection;
import com.rodrigo.tastyhub.modules.user.domain.repository.UserRepository;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import com.rodrigo.tastyhub.shared.kernel.annotations.FileCleanup;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ImageStorageService imageStorageService;

    private final SecurityService securityService;

    private final RoleService roleService;

    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    public UserProfileProjection findProfileById(Long userId, @Nullable Long requesterId) {
        return userRepository.findProfileById(userId, requesterId)
            .orElseThrow(() -> new ResourceNotFoundException("The user provided cannot be found"));
    }

    public User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("The user provided cannot be found"));
    }

    @Transactional
    public User createNewUser(SignupRequestDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new DomainException("This email is already in use!");
        }

        User user = new User();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(dto.email());
        user.setUsername(dto.email());

        user.setupPassword(dto.password(), passwordEncoder);

        user.setRoles(Set.of(roleService.getDefaultRole()));
        user.createDefaultCollections();

        user.initializeSettings();

        return userRepository.save(user);
    }

    @FileCleanup
    public UserSummaryDto updateProfilePicture(MultipartFile file, String alternativeText) {
        User user = securityService.getCurrentUser();
        String oldFileName = user.getProfilePictureUrl();

        String filename = imageStorageService.storeImage(file);

        user.setProfilePictureUrl(filename);
        user.setProfilePictureAlt(alternativeText);

        userRepository.save(user);

        if (oldFileName != null) {
            imageStorageService.deleteImage(oldFileName);
        }

        return UserMapper.toSummary(user);
    }
}
