package com.rodrigo.tastyhub.modules.collections.domain.service;

import com.rodrigo.tastyhub.modules.collections.application.dto.request.ListCollectionQuery;
import com.rodrigo.tastyhub.modules.collections.application.dto.request.UserCollectionRequest;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.CollectionCounts;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.CollectionPagination;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.UserCollectionResponseDto;
import com.rodrigo.tastyhub.modules.collections.application.mapper.UserCollectionMapper;
import com.rodrigo.tastyhub.modules.collections.domain.model.CollectionSortBy;
import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import com.rodrigo.tastyhub.modules.collections.domain.repository.UserCollectionRepository;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.service.UserService;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import com.rodrigo.tastyhub.shared.exception.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rodrigo.tastyhub.modules.collections.infrastructure.persistence.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserCollectionService {
    private final UserService userService;
    private final RecipeService recipeService;
    private final SecurityService securityService;
    private final ImageStorageService imageStorageService;
    private final UserCollectionRepository collectionRepository;

    @Transactional
    public UserCollection findByIdOrThrow(Long collectionId) {
        return collectionRepository.findById(collectionId)
            .orElseThrow(() -> new ResourceNotFoundException("Collection was not found"));
    }

    @Transactional
    public CollectionPagination listCollectionsByUserId(Long userId, ListCollectionQuery queries) {
        if (!userService.existsById(userId)) {
            throw new ResourceNotFoundException("User does not exist or could not be found");
        }

        Pageable pageable = PageRequest.of(
            queries.page(),
            queries.size(),
            buildSort(queries.sortBy(), queries.direction())
        );

        Long currentUserId = securityService.getCurrentUserOptional()
            .map(User::getId)
            .orElse(null);

        Page<UserCollection> page = collectionRepository.findAll(
            CollectionSpecifications.withFilters(userId, currentUserId, queries),
            pageable
        );

        List<UserCollectionResponseDto> collections = page.getContent()
            .stream()
            .map((collection) -> UserCollectionMapper.toDto(
                collection,
                getCollectionCountsById(collection.getId())
            ))
            .toList();

        PaginationMetadata metadata = new PaginationMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            queries.direction(),
            page.hasNext(),
            page.hasPrevious()
        );

        return new CollectionPagination(collections, metadata);
    }

    @RequiresVerification
    public UserCollectionResponseDto createCollection(UserCollectionRequest newData, MultipartFile file, String altText) {
        User user = securityService.getCurrentUserOptional()
            .orElseThrow(() -> new UnauthorizedException("You must be logged in"));

        UserCollection collection = UserCollection
            .builder()
            .name(newData.name())
            .user(user)
            .description(newData.description())
            .isFavorite(false)
            .isFixed(newData.isFixed())
            .isPublic(newData.isPublic())
            .isDeletable(true)
            .build();

        Optional.ofNullable(file).ifPresent(file1 -> {
            String filename = imageStorageService.storeImage(file);
            collection.setCoverUrl(filename);
            collection.setCoverAlt(altText);
        });

        return generateResponse(collectionRepository.save(collection));
    }

    @Retryable(
        retryFor = { ObjectOptimisticLockingFailureException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 500)
    )
    @Transactional
    public void favoriteRecipe(Long recipeId) {
        User user = securityService.getCurrentUser();
        Recipe recipe = recipeService.findByIdOrThrow(recipeId);

        UserCollection favoritesCollection = user.getFavoritesCollection();

        if (favoritesCollection == null) {
            throw new IllegalStateException("User favorites collection not initialized");
        }

        if (favoritesCollection.getRecipes().contains(recipe)) {
            throw new DomainException("Recipe is already in your favorites collection");
        }

        favoritesCollection.addRecipe(recipe);

        collectionRepository.saveAndFlush(favoritesCollection);
    }

    @Retryable(
        retryFor = { ObjectOptimisticLockingFailureException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 500)
    )
    @Transactional
    public void unfavoriteRecipe(Long recipeId) {
        User user = securityService.getCurrentUser();
        Recipe recipe = recipeService.findByIdOrThrow(recipeId);

        UserCollection favoritesCollection = user.getFavoritesCollection();

        if (favoritesCollection == null) {
            throw new IllegalStateException("User favorites collection not initialized");
        }

        if (!favoritesCollection.getRecipes().contains(recipe)) {
            throw new DomainException("Recipe is not in your favorites collection");
        }

        favoritesCollection.removeRecipe(recipe);

        collectionRepository.saveAndFlush(favoritesCollection);
    }

    @RequiresVerification
    @Transactional
    public void addRecipeToCollection(
        Long collectionId,
        Long recipeId
    ) {
        User user = securityService.getCurrentUser();
        Recipe recipe = recipeService.findByIdOrThrow(recipeId);

        UserCollection collection = this.findByIdOrThrow(collectionId);

        if (!collection.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not permitted to add recipe to this collection");
        }

        collection.addRecipe(recipe);

        collectionRepository.saveAndFlush(collection);
    }

    @RequiresVerification
    @Transactional
    public void removeRecipeFromCollection(
        Long collectionId,
        Long recipeId
    ) {
        User user = securityService.getCurrentUser();
        Recipe recipe = recipeService.findByIdOrThrow(recipeId);

        UserCollection collection = this.findByIdOrThrow(collectionId);

        if (!collection.getRecipes().contains(collection)) {
            throw new DomainException("Recipe is not in this collection");
        }

        if (!collection.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not permitted to remove recipe from this collection");
        }

        collection.removeRecipe(recipe);

        collectionRepository.saveAndFlush(collection);
    }

    private CollectionCounts getCollectionCountsById(Long collectionId) {
        return collectionRepository.getCollectionCountsById(collectionId)
            .orElse(new CollectionCounts(0, 0));
    }

    private UserCollectionResponseDto generateResponse(UserCollection collection) {
        CollectionCounts counts = getCollectionCountsById(collection.getId());

        return UserCollectionMapper.toDto(collection, counts);
    }

    @Transactional
    private Sort buildSort(CollectionSortBy sortBy, SortDirection direction) {
        String field = sortBy == CollectionSortBy.CREATED_AT
            ? "createdAt"
            : "name";

        return direction == SortDirection.ASC
            ? Sort.by(field).ascending()
            : Sort.by(field).descending();
    }
}
