package com.rodrigo.tastyhub.modules.comments.interfaces.rest;

import com.rodrigo.tastyhub.modules.comments.application.dto.request.ReviewRequestDto;
import com.rodrigo.tastyhub.modules.comments.application.dto.response.ReviewResponseDto;
import com.rodrigo.tastyhub.modules.comments.application.mapper.CommentMapper;
import com.rodrigo.tastyhub.modules.comments.domain.CommentService;
import com.rodrigo.tastyhub.modules.comments.domain.model.Comment;
import com.rodrigo.tastyhub.shared.dto.response.ErrorResponseDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(
    name = "Comments",
    description = ""
)
@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Post a review on a recipe", description = "Adds a comment and a rating to a specific recipe. Requires authentication.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review created successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"The rating must be between 1 and 5\", \"status\": 400, \"timestamp\": \"2026-03-07T14:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "User not authenticated",
            content = @Content(
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Full authentication is required to access this resource\", \"status\": 401}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Recipe not found",
            content = @Content(
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Recipe not found with the provided ID\", \"status\": 404}"
                )
            )
        )
    })
    @PostMapping("/recipe/{id}")
    public ResponseEntity<ReviewResponseDto> reviewRecipe(
        @Parameter(description = "ID of the recipe to comment", required = true, example = "1")
        @PathVariable("id")
        @Min(value = 1, message = "The recipe ID must be a positive number")
        Long recipeId,

        @RequestBody
        @Valid
        ReviewRequestDto reviewDto
    ) {
        Comment response = commentService.reviewRecipeById(recipeId, reviewDto);

        URI uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();

        return ResponseEntity.created(uri).body(CommentMapper.toReview(response));
    }
}
