package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.CreateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.ListRecipesQuery;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.UpdateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.RecipePagination;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.SummaryRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.*;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.RecipeRepository;
import com.rodrigo.tastyhub.modules.recipes.infrastructure.persistence.RecipeSpecification;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.service.TagService;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
            recipe.setCurrency(currency);
            recipe.setEstimatedCost(recipeDto.estimatedCost());
        }

        if (recipeDto.hasTags()) {
            List<Tag> tags = tagService.findAllById(recipeDto.tagIds());
            recipe.setTags(new HashSet<>(tags));
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
    public void deleteRecipeById(Long id) throws BadRequestException {
        if (id == null) {
            throw new BadRequestException("Id was not provided");
        }

        recipeRepository.deleteById(id);
    }

    @RequiresVerification
    @Transactional
    public FullRecipeDto updateRecipeById(Long recipeId, UpdateRecipeDto body) throws BadRequestException {
        Recipe recipe = findByIdOrThrow(recipeId);

        Optional.ofNullable(body.title()).ifPresent(recipe::setTitle);
        Optional.ofNullable(body.description()).ifPresent(recipe::setDescription);

        recipe.updateTiming(recipe.getCookTimeMin(), recipe.getCookTimeMax());

        Currency currency = null;

        if (body.currencyId() != null) {
            currency = currencyService.findById(body.currencyId());
        }

        recipe.updateMonetaryDetails(body.estimatedCost(), currency);

        //  se lists forem null, ignorar
        //  se lists forem arrays vazios, altera
        return null;
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
