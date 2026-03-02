package com.rodrigo.tastyhub.modules.recipes.interfaces.rest;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.CreateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.ListRecipesQuery;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.UpdateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.RecipePagination;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
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
import jakarta.validation.constraints.NotBlank;
import org.apache.coyote.BadRequestException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
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
        Recipe response = this.recipeService.findByIdOrThrow(id);
        return ResponseEntity.ok(RecipeMapper.toFullRecipeDto(response));
    }

    @Operation(
        summary = "List and Filter Recipes",
        description = """
            Provides a paginated list of recipes with support for complex filtering.
            You can filter by tags, categories, rating, cost, and ingredient count.
            The results can be sorted by relevance, popularity, or date.
        """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Recipes successfully retrieved",
            content = @Content(schema = @Schema(implementation = RecipePagination.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid filter parameters provided",
            content = @Content
        )
    })
    @GetMapping
    public ResponseEntity<RecipePagination> listRecipes(
        @ParameterObject @Valid ListRecipesQuery request
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

    @Operation(
        summary = "Delete a recipe",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Permanently removes a recipe from the platform. Only the author or an administrator can perform this action."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Recipe deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden: You do not have permission to delete this recipe"),
        @ApiResponse(responseCode = "404", description = "Recipe not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipeById(
            @PathVariable("id") Long id
    ) throws BadRequestException {
        this.recipeService.deleteRecipeById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Update an existing recipe",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = """
            Updates recipe details including titles, descriptions, and nested lists (steps, ingredients, tags). 
            The 'Orchestrated Sync' pattern is used: any steps or ingredients omitted from the request will be removed, 
            while new ones will be created.
        """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Recipe updated successfully",
            content = @Content(schema = @Schema(implementation = FullRecipeDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
        @ApiResponse(responseCode = "403", description = "Forbidden: You are not the owner of this recipe"),
        @ApiResponse(responseCode = "404", description = "Recipe not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
    @PutMapping("/{id}")
    public ResponseEntity<FullRecipeDto> updateRecipeById(
        @PathVariable("id") Long id,
        @RequestBody @Valid UpdateRecipeDto body
    ) throws BadRequestException {
        FullRecipeDto response = this.recipeService.updateRecipeById(id, body);
        return ResponseEntity.ok(response);
    }
}
