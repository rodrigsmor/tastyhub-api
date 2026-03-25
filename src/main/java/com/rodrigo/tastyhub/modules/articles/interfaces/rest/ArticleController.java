package com.rodrigo.tastyhub.modules.articles.interfaces.rest;

import com.rodrigo.tastyhub.modules.articles.application.dto.response.FullArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.usecases.GetArticleByIdUseCase;
import com.rodrigo.tastyhub.shared.dto.response.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Articles",
    description = "Article management routes. Here, you can create, update, list, read, and delete articles." +
        "Read routes are hybrid, while write routes are mandatory private (requiring JWT authentication and authorization to perform these writes)."
)
@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final GetArticleByIdUseCase getArticleById;

    public ArticleController(GetArticleByIdUseCase getArticleById) {
        this.getArticleById = getArticleById;
    }

    @Operation(
        summary = "Get Article by ID",
        description = "Retrieves Article details"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Article details retrieved successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Article ID was not provided or is a negative number",
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
            description = "Article not found with the provided ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "message": "Article not found with the provided ID",
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
    public ResponseEntity<FullArticleDto> getArticleById(
        @Parameter(description = "ID of the article to retrieve", required = true, example = "1")
        @PathVariable("id")
        @Min(value = 1, message = "The article ID must be a positive number")
        Long id
    ) {
        FullArticleDto response = this.getArticleById.execute(id);
        return ResponseEntity.ok(response);
    }
}
