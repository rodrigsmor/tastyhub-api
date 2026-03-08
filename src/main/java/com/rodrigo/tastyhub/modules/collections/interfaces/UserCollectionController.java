package com.rodrigo.tastyhub.modules.collections.interfaces;

import com.rodrigo.tastyhub.modules.collections.domain.model.UserCollection;
import com.rodrigo.tastyhub.modules.collections.domain.service.UserCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        summary = "Favorite a recipe",
        description = "Adds a recipe to the user's favorites collection and increments its favorite count."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Recipe favorited successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid recipe ID"),
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
    public ResponseEntity<Void> removeRecipeToFavorites(
        @Parameter(description = "ID of the recipe to unfavorite", required = true)
        @PathVariable("id")
        @Min(value = 1, message = "The recipe ID must be a positive number")
        Long recipeId
    ) {
        collectionService.unfavoriteRecipe(recipeId);
        return ResponseEntity.noContent().build();
    }
}
