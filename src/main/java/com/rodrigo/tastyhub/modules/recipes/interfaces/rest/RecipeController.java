package com.rodrigo.tastyhub.modules.recipes.interfaces.rest;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.CreateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(
    name = "Recipes",
    description = ""
)
@RestController
@RequestMapping("/api/recipe")
public class RecipeController {
    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @Operation(
        summary = "Create a new recipe",
            security = { @SecurityRequirement(name = "bearerAuth") },
            description = "Registers a new recipe with its instructions, ingredients, and tags. " +
                "The user must be verified to perform this action."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Recipe created successfully",
            content = @Content(schema = @Schema(implementation = FullRecipeDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or missing required fields",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "User not verified or unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping
    public ResponseEntity<FullRecipeDto> createRecipe(
        @RequestBody @Valid CreateRecipeDto recipeDto
    ) throws BadRequestException {
        FullRecipeDto fullRecipeDto = this.recipeService.createRecipe(recipeDto);

        URI uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(fullRecipeDto.id())
            .toUri();

        return ResponseEntity.created(uri).body(fullRecipeDto);
    }
}
