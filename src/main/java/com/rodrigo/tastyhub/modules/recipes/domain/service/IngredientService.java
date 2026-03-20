package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.domain.model.Ingredient;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.IngredientRepository;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
}
