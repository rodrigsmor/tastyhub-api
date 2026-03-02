package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.*;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.RecipePagination;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.SummaryRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.*;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Currency;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.RecipeRepository;
import com.rodrigo.tastyhub.modules.recipes.infrastructure.persistence.RecipeSpecification;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final TagService tagService;
    private final SecurityService securityService;
    private final RecipeRepository recipeRepository;
    private final CurrencyService currencyService;
    private final IngredientService ingredientService;

    public Long getRecipesCountByUserId(Long userId) {
        return recipeRepository.countByAuthorId(userId);
    }

    public Recipe findByIdOrThrow(Long recipeId) {
        return recipeRepository.findById(recipeId)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with the provided ID"));
    }

    @RequiresVerification
    @Transactional
    public FullRecipeDto createRecipe(CreateRecipeDto recipeDto) throws BadRequestException {
        if (!recipeDto.hasSteps()) {
            throw new BadRequestException("Recipe must have at least one step");
        }

        User user = this.securityService.getCurrentUser();

        Recipe recipe = Recipe
            .builder()
            .title(recipeDto.title())
            .category(recipeDto.category())
            .description(recipeDto.description())
            .cookTimeMin(recipeDto.cookTimeMin())
            .cookTimeMax(recipeDto.cookTimeMax())
            .author(user)
            .build();

        if (recipeDto.hasCurrency()) {
            Currency currency = currencyService.findById(recipeDto.currencyId());
            recipe.updateMonetaryDetails(recipeDto.estimatedCost(), currency);
        }

        if (recipeDto.hasTags()) {
            this.syncTags(recipe, recipeDto.tagIds());
        }

        recipeDto.steps().forEach(stepDto -> {
            PreparationStep step = PreparationStep.builder()
                .stepNumber(stepDto.stepNumber())
                .instruction(stepDto.instruction())
                .build();

            recipe.addStep(step);
        });

        recipeDto.ingredients().forEach(ingredientDto -> {
            Optional<Ingredient> ingredient = ingredientService.findById(ingredientDto.ingredientId());
            ingredient.ifPresent(value -> recipe.addIngredient(value, ingredientDto.quantity(), ingredientDto.unit()));
        });

        Recipe savedRecipe = recipeRepository.save(recipe);

        return RecipeMapper.toFullRecipeDto(savedRecipe);
    }

    @Transactional
    public RecipePagination listRecipes(ListRecipesQuery request) {
        Pageable pageable = PageRequest.of(
            request.page(),
            request.size(),
            buildSort(request.sortBy(), request.direction())
        );

        Page<Recipe> page = recipeRepository.findAll(
            RecipeSpecification.withFilters(request),
            pageable
        );

        System.out.println(page);

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

    @RequiresVerification
    @Transactional
    public FullRecipeDto updateRecipeById(Long recipeId, UpdateRecipeDto newData) throws BadRequestException {
        Recipe recipe = findByIdOrThrow(recipeId);

        Optional.ofNullable(newData.title()).ifPresent(recipe::setTitle);
        Optional.ofNullable(newData.description()).ifPresent(recipe::setDescription);

        recipe.updateTiming(recipe.getCookTimeMin(), recipe.getCookTimeMax());

        Currency currency = null;

        if (newData.currencyId() != null) {
            currency = currencyService.findById(newData.currencyId());
        }

        recipe.updateMonetaryDetails(newData.estimatedCost(), currency);

        Optional.ofNullable(newData.tagIds())
            .ifPresent(ids -> this.syncTags(recipe, ids));

        if (newData.steps() != null) {
            this.syncSteps(recipe, newData.steps());
        };

        if (newData.steps() != null) {
            this.syncSteps(recipe, newData.steps());
        }

        if (newData.ingredients() != null) {
            this.syncIngredients(recipe, newData.ingredients());
        }

        return null;
    }

    private void syncIngredients(Recipe recipe, List<UpdateRecipeIngredientDto> dtos) throws BadRequestException {
        if (dtos == null || dtos.isEmpty()) {
            throw new BadRequestException("Recipe must have at least one ingredient!");
        }

        Set<Long> dtoIds = dtos.stream()
            .map(UpdateRecipeIngredientDto::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        recipe.getIngredients().removeIf(existing -> !dtoIds.contains(existing.getId()));

        for (var dto : dtos) {
            if (dto.id() != null) {
                recipe.getIngredients().stream()
                    .filter(ri -> ri.getId().equals(dto.id()))
                    .findFirst()
                    .ifPresent(ri -> {
                        ri.setQuantity(dto.quantity());
                        ri.setUnit(dto.unit());
                    });
            } else {
                Ingredient masterIngredient = ingredientService.findById(dto.ingredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient " + dto.ingredientId() + " not found"));

                RecipeIngredient newRelation = RecipeIngredient.builder()
                    .ingredient(masterIngredient)
                    .quantity(dto.quantity())
                    .unit(dto.unit())
                    .recipe(recipe)
                    .build();

                recipe.getIngredients().add(newRelation);
            }
        }
    }

    @Transactional
    private void syncSteps(Recipe recipe, List<UpdatePreparationStepDto> stepDtos) throws BadRequestException {
        if (stepDtos.isEmpty()) {
            throw new BadRequestException("Recipe must have at least one preparation step");
        }

        Set<Long> dtoIds = stepDtos.stream()
            .map(UpdatePreparationStepDto::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        recipe.getSteps().removeIf(existingStep -> !dtoIds.contains(existingStep.getId()));

        for (var dto : stepDtos) {
            if (dto.id() != null) {
                recipe.getSteps().stream()
                    .filter(s -> s.getId().equals(dto.id()))
                    .findFirst()
                    .ifPresent(existingStep -> {
                        existingStep.setStepNumber(dto.stepNumber());
                        existingStep.setInstruction(dto.instruction());
                    });
            } else {
                PreparationStep newStep = new PreparationStep();
                newStep.setStepNumber(dto.stepNumber());
                newStep.setInstruction(dto.instruction());
                recipe.addStep(newStep);
            }
        }
    }

    private void syncTags(Recipe recipe, Set<Long> tagIds) {
        recipe.getTags().clear();

        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagService.findAllById(tagIds);

            if (tags.size() != tagIds.size()) {
                throw new ResourceNotFoundException("One or more provided Tag IDs do not exist.");
            }

            recipe.getTags().addAll(tags);
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

    // CREATE, READ, UPDATE, DELETE
}
