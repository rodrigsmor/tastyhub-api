package com.rodrigo.tastyhub.modules.collections.domain.service;

import com.rodrigo.tastyhub.modules.collections.application.dto.request.UserCollectionRequest;
import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import com.rodrigo.tastyhub.modules.collections.domain.repository.UserCollectionRepository;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserCollectionService {
    private final SecurityService securityService;
    private final ImageStorageService imageStorageService;
    private final UserCollectionRepository collectionRepository;

    @RequiresVerification
    public UserCollection createCollectionByUserId(UserCollectionRequest newData, MultipartFile file, String altText) {
        User user = securityService.getCurrentUserOptional()
            .orElseThrow(() -> new UnauthorizedException("You must be logged in"));

        UserCollection collection = UserCollection
            .builder()
            .name(newData.name())
            .user(user)
            .description(newData.description())
            .isFavorite(newData.isFavorite())
            .isFixed(newData.isFixed())
            .isPublic(newData.isPublic())
            .isDeletable(newData.isDeletable())
            .build();

        Optional.ofNullable(file).ifPresent(file1 -> {
            String filename = imageStorageService.storeImage(file);
            collection.setCoverUrl(filename);
            collection.setCoverAlt(altText);
        });

        return collectionRepository.save(collection);
    }
}
