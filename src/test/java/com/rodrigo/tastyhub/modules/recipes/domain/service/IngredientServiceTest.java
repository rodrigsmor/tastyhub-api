package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.domain.model.Ingredient;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.IngredientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IngredientServiceTest {
    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientService ingredientService;

    @Nested
    @DisplayName("Testes for Find By Id Method")
    class FindByIdTests {
        @Test
        @DisplayName("Should return empty ingredient when is not found")
        void shouldReturnEmptyIngredientWhenIsNotFound() {
            Long ingredientId = 1L;

            when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.empty());

            Optional<Ingredient> result = ingredientService.findById(ingredientId);

            assertEquals(Optional.empty(),result);
            verify(ingredientRepository, times(1)).findById(eq(ingredientId));
        }

        @Test
        @DisplayName("Should return ingredient successfully")
        void shouldReturnIngredientSuccessfully() {
            Long ingredientId = 1L;

            Ingredient ingredient = Ingredient
                .builder()
                .id(ingredientId)
                .name("Mock Ingredient")
                .build();

            when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.ofNullable(ingredient));

            Optional<Ingredient> result = ingredientService.findById(ingredientId);

            assertNotNull(result);
            assert ingredient != null;
            assertEquals(Optional.of(ingredient), result);
            verify(ingredientRepository, times(1)).findById(eq(ingredientId));
        }
    }
}