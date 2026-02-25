package com.rodrigo.tastyhub.modules.recipes.interfaces.rest;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.CreateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {
    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    public ResponseEntity<CreateRecipeDto> createRecipe(
        @RequestBody CreateRecipeDto recipeDto
    ) throws BadRequestException {
        FullRecipeDto fullRecipeDto = this.recipeService.createRecipe(recipeDto);

        URI uri = URI.create(ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/api/auth/signup")
            .toUriString()
        );

        return ResponseEntity.created(uri).body(recipeDto);
    }
}
