package com.rodrigo.tastyhub.modules.recipes.interfaces.rest;

import com.rodrigo.tastyhub.modules.recipes.application.dto.request.CreateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.ListRecipesQuery;
import com.rodrigo.tastyhub.modules.recipes.application.dto.request.UpdateRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.FullRecipeDto;
import com.rodrigo.tastyhub.modules.recipes.application.dto.response.RecipePagination;
import com.rodrigo.tastyhub.modules.recipes.application.mapper.RecipeMapper;
import com.rodrigo.tastyhub.modules.recipes.application.usecases.*;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import com.rodrigo.tastyhub.shared.dto.response.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(
    name = "Recipes",
    description = "Recipe management routes. Here, you can create, update, list, read, and delete recipes." +
        "Read routes are hybrid, while write routes are mandatory private (requiring JWT authentication and authorization to perform these writes)."
)
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;
    private final ListRecipesUseCase listRecipes;
    private final UpdateRecipeUseCase updateRecipe;
    private final CreateRecipeUseCase createRecipe;
    private final GetRecipeByIdUseCase getRecipeById;
    private final ListRecipesByCollectionUseCase listRecipesByCollection;

    public RecipeController(
        RecipeService recipeService,
        ListRecipesUseCase listRecipes,
        CreateRecipeUseCase createRecipe,
        GetRecipeByIdUseCase getRecipeById,
        UpdateRecipeUseCase updateRecipe,
        ListRecipesByCollectionUseCase listRecipesByCollection
    ) {
        this.recipeService = recipeService;
        this.listRecipes = listRecipes;
        this.createRecipe = createRecipe;
        this.getRecipeById = getRecipeById;
        this.updateRecipe = updateRecipe;
        this.listRecipesByCollection = listRecipesByCollection;
    }

    @Operation(
        summary = "Get Recipe by ID",
        description = "Retrieves Recipe details"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Recipe details retrieved successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Recipe ID was not provided or is a negative number",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                value = """
                        {
                          "message": "The recipe ID must be a positive number",
                          "status": 400,
                          "timestamp": "2026-08-15T13:15:36"
                        }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Recipe not found with the provided ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "message": "Recipe not found with the provided ID",
                          "status": 404,
                          "timestamp": "2026-03-07T12:05:00"
                        }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "An unexpected error occurred while processing the request",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                value = """
                        {
                          "message": "An unexpected error occurred while processing the request",
                          "status": 500,
                          "timestamp": "2026-03-12T12:30:00"
                        }
                    """
                )
            )
        ),
    })
    @GetMapping("/{id}")
    public ResponseEntity<FullRecipeDto> getRecipeById(
        @Parameter(description = "ID of the recipe to retrieve", required = true, example = "1")
        @PathVariable("id")
        @Min(value = 1, message = "The recipe ID must be a positive number")
        Long id
    ) {
        FullRecipeDto response = this.getRecipeById.execute(id);
        return ResponseEntity.ok(response);
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
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                value = """
                    {
                      "message": "Invalid filter parameters provided",
                      "status": 400,
                      "timestamp": "2026-03-12T12:30:00"
                    }
                    """
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<RecipePagination> listRecipes(
        @ParameterObject @Valid ListRecipesQuery request
    ) {
        RecipePagination response = this.listRecipes.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/collections/{collectionId}")
    public ResponseEntity<RecipePagination> listRecipesByCollection(
        @Parameter(description = "ID of the collection to retrieve recipes", required = true, example = "1")
        @PathVariable("collectionId")
        @Min(value = 1, message = "The collection ID must be a positive number")
        Long collectionId,

        @ParameterObject
        @Valid
        ListRecipesQuery request
    ) {
        RecipePagination response = this.listRecipesByCollection.execute(collectionId, request);
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
    ) {
        FullRecipeDto fullRecipeDto = this.createRecipe.execute(recipeDto);

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
    ) {
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
    ) {
        FullRecipeDto response = this.updateRecipe.execute(id, body);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update cover of Recipe",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Uploads a new cover and updates the alternative text. The previous file will be replaced."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Recipe's cover updated successfully",
            content = @Content(schema = @Schema(implementation = UserSummaryDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "500", description = "Internal server error during file storage")
    })
    @PatchMapping(
        value = "/{id}/cover",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FullRecipeDto> updateRecipeCover(
        @PathVariable("id") Long id,

        @Parameter(description = "Profile picture file")
        @RequestPart(value = "file") MultipartFile file,

        @Parameter(description = "Profile picture alternative text")
        @RequestPart(value = "alternative_text", required = false) String alternativeText
    ) {
        Recipe recipe = this.recipeService.updateCoverById(id, file, alternativeText);

        URI uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}/cover")
            .buildAndExpand(recipe.getId())
            .toUri();

        return ResponseEntity.created(uri).body(RecipeMapper.toFullRecipeDto(recipe));
    }
}
