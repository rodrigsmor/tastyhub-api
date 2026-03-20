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
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final SecurityService securityService;
    private final RecipeRepository recipeRepository;
    private final IngredientService ingredientService;
    private final ImageStorageService imageStorageService;

    public Long getCountByUserId(Long userId) {
        return recipeRepository.countByAuthorId(userId);
    }

    public Recipe findByIdOrThrow(Long recipeId) {
        return recipeRepository.findById(recipeId)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with the provided ID"));
    }

    public RecipePagination getRecipesList(
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
                collection == null ? null : collection.getId()
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

    public Recipe updateAndSync(
        Recipe newRecipe,
        List<UpdateRecipeIngredientDto> newIngredients,
        List<UpdatePreparationStepDto> newSteps
    ) {
        if (newSteps != null) {
            this.syncSteps(newRecipe, newSteps);
        }

        if (newIngredients != null) {
            this.syncIngredients(newRecipe, newIngredients);
        }

        return recipeRepository.save(newRecipe);
    }

    @RequiresVerification
    @Transactional
    public void deleteRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));

        Long currentUserId = securityService.getCurrentUser().getId();

        if (!recipe.getAuthor().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to delete this recipe");
        }

        recipeRepository.delete(recipe);
    }

    @FileCleanup
    @Transactional
    public Recipe updateCoverById(Long recipeId, MultipartFile file, String alternativeText) {
        Long userId = securityService.getCurrentUser().getId();

        Recipe recipe = findByIdOrThrow(recipeId);

        if (!recipe.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to update this recipe");
        }

        String oldFileName = recipe.getCoverUrl();

        String filename = imageStorageService.storeImage(file);

        recipe.setCoverUrl(filename);
        recipe.setCoverAlt(alternativeText);

        recipeRepository.save(recipe);

        if (oldFileName != null) {
            imageStorageService.deleteImage(oldFileName);
        }

        return recipe;
    }

    private void syncIngredients(Recipe recipe, List<UpdateRecipeIngredientDto> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new DomainException("Recipe must have at least one ingredient!");
        }

        Set<Long> ingredientIds = ingredients.stream()
            .map(UpdateRecipeIngredientDto::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        recipe.getIngredients().removeIf(existing -> !ingredientIds.contains(existing.getId()));

        for (var ingredient : ingredients) {
            if (ingredient.id() != null) {
                recipe.getIngredients().stream()
                    .filter(ri -> ri.getId().equals(ingredient.id()))
                    .findFirst()
                    .ifPresent(ri -> {
                        ri.setQuantity(ingredient.quantity());
                        ri.setUnit(ingredient.unit());
                    });
            } else {
                Ingredient masterIngredient = ingredientService.findById(ingredient.ingredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient " + ingredient.ingredientId() + " not found"));

                RecipeIngredient newRelation = RecipeIngredient.builder()
                    .ingredient(masterIngredient)
                    .quantity(ingredient.quantity())
                    .unit(ingredient.unit())
                    .recipe(recipe)
                    .build();

                recipe.getIngredients().add(newRelation);
            }
        }
    }

    @Transactional
    private void syncSteps(Recipe recipe, List<UpdatePreparationStepDto> newSteps) {
        if (newSteps == null || newSteps.isEmpty()) {
            throw new IllegalArgumentException("Recipe must have at least one preparation step");
        }

        recipe.getSteps().clear();

        recipeRepository.flush();

        int stepNumber = 1;

        for (var dto : newSteps) {
            PreparationStep step = new PreparationStep();
            step.setStepNumber(stepNumber++);
            step.setInstruction(dto.instruction());

            recipe.addStep(step);
        }
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
