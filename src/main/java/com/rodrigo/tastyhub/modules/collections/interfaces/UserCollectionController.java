package com.rodrigo.tastyhub.modules.collections.interfaces;

import com.rodrigo.tastyhub.modules.collections.application.dto.request.ListCollectionQuery;
import com.rodrigo.tastyhub.modules.collections.application.dto.request.UserCollectionRequest;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.CollectionPagination;
import com.rodrigo.tastyhub.modules.collections.application.dto.response.UserCollectionResponseDto;
import com.rodrigo.tastyhub.modules.collections.domain.service.UserCollectionService;
import com.rodrigo.tastyhub.shared.dto.response.ErrorResponseDto;
import com.rodrigo.tastyhub.shared.kernel.annotations.RequiresProfileAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    name = "User Collections",
    description = "Operations related to user's personal recipe collections and favorites"
)
@RestController
@RequestMapping("/api/collections")
public class UserCollectionController {
    private final UserCollectionService collectionService;

    public UserCollectionController(UserCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @Operation(
        summary = "List collections by user ID",
        description = """
            Provides a paginated list of collections belonging to a specific user. 
            Supports filtering by name/description (query) and visibility status.
            Public collections are visible to everyone, while private collections 
            are only returned if the requester is the owner.
        """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Collections successfully retrieved",
            content = @Content(schema = @Schema(implementation = CollectionPagination.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid filter or pagination parameters provided",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "message": "Invalid filter or pagination parameters provided",
                          "status": 400,
                          "timestamp": "2026-03-08T17:30:00"
                        }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "message": "User does not exist or could not be found",
                      "status": 404,
                      "timestamp": "2026-03-08T17:30:00"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/user/{userId}")
    @RequiresProfileAccess
    public ResponseEntity<CollectionPagination> listCollectionsByUserId(
        @Parameter(description = "ID of the user", required = true)
        @PathVariable("userId")
        @Min(value = 1, message = "The User ID must be a positive number")
        Long userId,

        @ParameterObject
        @Valid
        ListCollectionQuery queries
    ) {
        CollectionPagination result = collectionService.listCollectionsByUserId(userId, queries);
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Create a new collection",
        description = "Creates a collection with an optional cover image. Sent as multipart/form-data."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Collection created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data provided")
    })
    @PostMapping(name = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserCollectionResponseDto> createCollection(
        @Valid @ModelAttribute UserCollectionRequest newData,

        @Parameter(description = "Cover image file")
        @RequestPart(value = "cover", required = false) MultipartFile file,

        @Parameter(description = "Alternative text for accessibility")
        @RequestPart(value = "alternative_text", required = false) String alternativeText
    ) {
        UserCollectionResponseDto response = collectionService.createCollection(newData, file, alternativeText);

        URI uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @Operation(
        summary = "Favorite a recipe",
        description = "Adds a recipe to the user's favorites collection and increments its favorite count."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Recipe favorited successfully"),
        @ApiResponse(responseCode = "400", description = "Recipe is already in your favorites collection"),
        @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    @PutMapping("/recipe/{id}/favorite")
    public ResponseEntity<Void> addRecipeToFavorites(
        @Parameter(description = "ID of the recipe to favorite", required = true)
        @PathVariable("id")
        @Min(value = 1, message = "The recipe ID must be a positive number")
        Long recipeId
    ) {
        collectionService.favoriteRecipe(recipeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Unfavorite a recipe",
        description = "Removes a recipe from the user's favorites collection and decrements its favorite count."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Recipe unfavorited successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid recipe ID"),
        @ApiResponse(responseCode = "404", description = "Recipe not found")
    })
    @PutMapping("/recipe/{id}/unfavorite")
    public ResponseEntity<Void> removeRecipeFromFavorites(
        @Parameter(description = "ID of the recipe to unfavorite", required = true)
        @PathVariable("id")
        @Min(value = 1, message = "The recipe ID must be a positive number")
        Long recipeId
    ) {
        collectionService.unfavoriteRecipe(recipeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Add a recipe to a collection",
        description = """
            Adds a specific recipe to a user's collection. 
            The requester must be the owner of the collection. 
            Custom collections allow multiple recipes, but duplicate entries are generally ignored.
        """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Recipe successfully added to the collection"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid ID provided or recipe already exists in the collection",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "You do not have permission to modify this collection",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Collection or Recipe not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    @PutMapping("/{collectionId}/recipe/{recipeId}")
    public ResponseEntity<Void> addRecipeToCollection(
        @Parameter(description = "ID of the target collection", example = "1", required = true)
        @PathVariable("collectionId")
        @Min(value = 1, message = "The collection ID must be a positive number")
        Long collectionId,

        @Parameter(description = "ID of the recipe to be added", example = "42", required = true)
        @PathVariable("recipeId")
        @Min(value = 1, message = "The recipe ID must be a positive number")
        Long recipeId
    ) {
        collectionService.addRecipeToCollection(collectionId, recipeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Remove a recipe from a collection",
        description = """
            Removes the association between a specific recipe and a collection. 
            The requester must be the owner of the collection. 
            If the recipe is not in the collection, a 404 or 400 error will be returned.
        """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Recipe successfully removed from the collection"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid ID provided",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "You do not have permission to modify this collection",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Collection or Recipe not found in this collection",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    @DeleteMapping("/{collectionId}/recipe/{recipeId}")
    public ResponseEntity<Void> removeRecipeFromCollection(
        @Parameter(description = "ID of the target collection", example = "1", required = true)
        @PathVariable("collectionId")
        @Min(value = 1, message = "The collection ID must be a positive number")
        Long collectionId,

        @Parameter(description = "ID of the recipe to be removed", example = "42", required = true)
        @PathVariable("recipeId")
        @Min(value = 1, message = "The recipe ID must be a positive number")
        Long recipeId
    ) {
        collectionService.removeRecipeFromCollection(collectionId, recipeId);
        return ResponseEntity.noContent().build();
    }

}
