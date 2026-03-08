package com.rodrigo.tastyhub.modules.collections.application.mapper;

import com.rodrigo.tastyhub.modules.collections.application.dto.response.CollectionCounts;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.UserCollectionResponseDto;
import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserCollectionMapper {
    private static ImageStorageService storageService;

    @Autowired
    public void setStorageService(ImageStorageService storageService) {
        UserCollectionMapper.storageService = storageService;
    }

    public static UserCollectionResponseDto toDto(UserCollection collection, CollectionCounts counts) {
        long recipes = counts.recipeCounts();
        long articles = counts.articleCounts();

        return new UserCollectionResponseDto(
            collection.getId(),
            collection.getName(),
            collection.getDescription(),
            storageService.generateImageUrl(collection.getCoverUrl()),
            collection.getCoverAlt(),
            collection.isFavorite(),
            collection.isFixed(),
            collection.isPublic(),
            collection.isDeletable(),
            recipes + articles,
            recipes,
            articles,
            collection.getCreatedAt(),
            collection.getUpdatedAt()
        );
    }
}
