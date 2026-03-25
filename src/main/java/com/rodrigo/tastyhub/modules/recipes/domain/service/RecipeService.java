package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.*;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.RecipePagination;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.SummaryRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.*;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.RecipeRepository;
import com.rodrigo.tastyhub.modules.recipes.infrastructure.persistence.RecipeSpecification;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import com.rodrigo.tastyhub.shared.kernel.annotations.FileCleanup;
import jakarta.annotation.Nullable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;

    public Long getCountByUserId(Long userId) {
        return recipeRepository.countByAuthorId(userId);
    }

    public Recipe findByIdOrThrow(Long recipeId) {
        return recipeRepository.findById(recipeId)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with the provided ID"));
    }

    public RecipePagination findAll(
        ListRecipesQuery request,
        @Nullable User owner,
        @Nullable UserCollection collection
    ) {
        Pageable pageable = PageRequest.of(
            request.page(),
            request.size(),
            buildSort(request.sortBy(), request.direction())
        );

        Page<Recipe> page = recipeRepository.findAll(
            RecipeSpecification.withFilters(
                request,
                collection == null ? null : collection.getId(),
                owner != null ? owner.getId() : null
            ),
            pageable
        );

        List<SummaryRecipeDto> recipes = page.getContent()
            .stream()
            .map(RecipeMapper::toSummaryDto)
            .toList();

        PaginationMetadata metadata = new PaginationMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            request.direction(),
            page.hasNext(),
            page.hasPrevious()
        );

        return new RecipePagination(recipes, metadata);
    }

    public Recipe create(Recipe newRecipe) {
        if (newRecipe.getSteps().isEmpty()) {
            throw new IllegalArgumentException("Recipe must have at least one step!");
        }

        if (newRecipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Recipe must have at least one ingredient!");
        }

        return recipeRepository.save(newRecipe);
    }

    public Recipe update(Recipe newRecipe) {
        return recipeRepository.save(newRecipe);
    }

    @RequiresVerification
    @Transactional
    public void deleteById(Long recipeId, Long ownerId) {
        Recipe recipe = this.findByIdOrThrow(recipeId);

        recipe.validateOwnership(ownerId);

        recipeRepository.delete(recipe);
    }

    @FileCleanup
    @Transactional
    public Recipe updateCoverById(
        Long recipeId,
        User owner,
        String newCoverUrl,
        String newAlternativeText
    ) {
        Recipe recipe = this.findByIdOrThrow(recipeId);

        recipe.validateOwnership(owner.getId());

        recipe.updateCover(newCoverUrl, newAlternativeText);

        return recipeRepository.save(recipe);
    }

    @Transactional
    private Sort buildSort(RecipeSortBy sortBy, SortDirection direction) {
        String field = switch (sortBy) {
            case TITLE -> "title";
            case REVIEWS -> "statistics.averageRating";
            case RELEVANCE -> "statistics.favoritesCount";
            default -> "createdAt";
        };

        return direction == SortDirection.ASC
            ? Sort.by(field).ascending()
            : Sort.by(field).descending();
    }
}
