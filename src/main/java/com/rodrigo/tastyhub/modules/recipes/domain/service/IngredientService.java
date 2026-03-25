package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.domain.model.Ingredient;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeIngredient;
import com.rodrigo.tastyhub.modules.recipes.domain.model.RecipeIngredientCommand;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.IngredientRepository;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    public Optional<Ingredient> findById(Long id) {
        return this.ingredientRepository.findById(id);
    }

    public Ingredient findByIdOrThrow(Long id) {
        return this.ingredientRepository.findById(id)
            .orElseThrow(() -> new DomainException("Ingredient couldn't be found!"));
    }

    public List<RecipeIngredient> preparerAll(List<RecipeIngredientCommand> recipeIngredients, Recipe recipe) {
        if (recipeIngredients == null || recipeIngredients.isEmpty()) {
            throw new IllegalArgumentException("It must have at least one ingredient!");
        }

        return recipeIngredients.stream().map(recipeIngredient -> {
            if (recipeIngredient.id() != null) {
                return RecipeIngredient.builder()
                    .id(recipeIngredient.id())
                    .quantity(recipeIngredient.quantity())
                    .unit(recipeIngredient.unit())
                    .recipe(recipe)
                    .build();
            } else {
                Ingredient ingredient = this.findById(recipeIngredient.ingredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));

                return RecipeIngredient.builder()
                    .ingredient(ingredient)
                    .quantity(recipeIngredient.quantity())
                    .unit(recipeIngredient.unit())
                    .recipe(recipe)
                    .build();
            }
        }).collect(Collectors.toList());
    }
}
