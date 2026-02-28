package com.rodrigo.tastyhub.modules.recipes.interfaces.rest;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.CreateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.ListRecipesRequest;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.RecipePagination;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.shared.dto.response.ErrorResponseDto;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

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
        summary = "Get Recipe by ID",
        description = "Retrieves Recipe details"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recipe details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Recipe not found with the provided ID"),
        @ApiResponse(responseCode = "500", description = "An unexpected error occurred while processing the request"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<FullRecipeDto> getRecipeById(@PathVariable("id") Long id) {
        FullRecipeDto response = this.recipeService.getRecipeById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Retrieve a list of recipes",
        description = ""
    )
    @GetMapping
    public ResponseEntity<RecipePagination> listAllRecipes(
        @ModelAttribute ListRecipesRequest request
    ) {
        RecipePagination response = this.recipeService.listRecipes(request);
        return ResponseEntity.ok(response);
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
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or missing required fields",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "User not verified or unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
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
