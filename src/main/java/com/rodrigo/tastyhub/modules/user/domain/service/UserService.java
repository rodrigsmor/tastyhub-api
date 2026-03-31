package com.rodrigo.tastyhub.modules.user.domain.service;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import com.rodrigo.tastyhub.modules.user.application.mapper.UserMapper;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.projections.UserProfileProjection;
import com.rodrigo.tastyhub.modules.user.domain.repository.UserRepository;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import com.rodrigo.tastyhub.shared.kernel.annotations.FileCleanup;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private final ImageStorageService imageStorageService;

    private final SecurityService securityService;

    public List<User> findAllByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> uniqueIds = new HashSet<>(userIds);
        List<User> foundUsers = userRepository.findAllById(uniqueIds);

        if (foundUsers.size() != uniqueIds.size()) {
            Set<Long> foundIds = foundUsers.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

            List<Long> missingIds = uniqueIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

            throw new ResourceNotFoundException("Users not found for IDs: " + missingIds);
        }

        return foundUsers;
    }

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
