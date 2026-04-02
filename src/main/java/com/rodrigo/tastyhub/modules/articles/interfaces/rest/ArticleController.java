package com.rodrigo.tastyhub.modules.articles.interfaces.rest;

import com.rodrigo.tastyhub.modules.articles.application.dto.request.CreateArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.dto.request.UpdateArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.dto.response.ArticlePaginationDto;
import com.rodrigo.tastyhub.modules.articles.application.dto.response.FullArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.dto.response.ListArticlesQuery;
import com.rodrigo.tastyhub.modules.articles.application.usecases.*;
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
    name = "Articles",
    description = "Article management routes. Here, you can create, update, list, read, and delete articles." +
        "Read routes are hybrid, while write routes are mandatory private (requiring JWT authentication and authorization to perform these writes)."
)
@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final CreateArticleUseCase createArticle;
    private final GetArticleByIdUseCase getArticleById;
    private final ListArticlesUseCase listArticles;
    private final UpdateArticleByIdUseCase updateArticleById;
    private final DeleteArticleByIdUseCase deleteArticleById;
    private final UpdateArticleCoverUseCase updateArticleCover;

    public ArticleController(
        ListArticlesUseCase listArticles,
        GetArticleByIdUseCase getArticleById,
        CreateArticleUseCase createArticle,
        UpdateArticleByIdUseCase updateArticleById,
        DeleteArticleByIdUseCase deleteArticleById,
        UpdateArticleCoverUseCase updateArticleCover
    ) {
        this.createArticle = createArticle;
        this.getArticleById = getArticleById;
        this.listArticles = listArticles;
        this.updateArticleById = updateArticleById;
        this.deleteArticleById = deleteArticleById;
        this.updateArticleCover = updateArticleCover;
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
                          "message": "The Article ID must be a positive number",
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

    @Operation(
        summary = "Create a new Article",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Registers a new article with its title, content, visibility and language." +
            "The user must be verified to perform this action."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Article created successfully",
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
    public ResponseEntity<FullArticleDto> createArticle(
        @RequestBody @Valid CreateArticleDto articleDto
    ) {
        FullArticleDto response = this.createArticle.execute(articleDto);

        URI uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @Operation(
        summary = "List and Filter Articles",
        description = """
            Provides a paginated list of Articles with support for complex filtering.
            You can filter by tags, categories, rating, cost, and ingredient count.
            The results can be sorted by relevance, popularity, or date.
        """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Articles successfully retrieved",
            content = @Content(schema = @Schema(implementation = ArticlePaginationDto.class))
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
    public ResponseEntity<ArticlePaginationDto> listArticles(
        @ParameterObject @Valid ListArticlesQuery request
    ) {
        ArticlePaginationDto response = this.listArticles.execute(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update an existing Article",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Updates article details including titles, content, visibility and language."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Article updated successfully",
            content = @Content(schema = @Schema(implementation = FullArticleDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
        @ApiResponse(responseCode = "403", description = "Forbidden: You are not the owner of this article"),
        @ApiResponse(responseCode = "404", description = "Article not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<FullArticleDto> updateArticleById(
        @PathVariable("id") Long id,
        @RequestBody @Valid UpdateArticleDto body
    ) {
        FullArticleDto response = this.updateArticleById.execute(id, body);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update cover of Article",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Uploads a new cover and updates the alternative text. The previous file will be replaced."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Article's cover updated successfully",
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
    public ResponseEntity<FullArticleDto> updateCover(
        @PathVariable("id") Long id,

        @Parameter(description = "New Article Cover file")
        @RequestPart(value = "file") MultipartFile file,

        @Parameter(description = "Article alternative text")
        @RequestPart(value = "alternative_text", required = false) String alternativeText
    ) {
        FullArticleDto recipe = this.updateArticleCover.execute(id, file, alternativeText);

        URI uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}/cover")
            .buildAndExpand(recipe.id())
            .toUri();

        return ResponseEntity.created(uri).body(recipe);
    }

    @Operation(
        summary = "Delete an Article",
        security = { @SecurityRequirement(name = "bearerAuth") },
        description = "Permanently removes an article from the platform. Only the author or an administrator can perform this action."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Article deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden: You do not have permission to delete this article"),
        @ApiResponse(responseCode = "404", description = "Article not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticleById(
            @PathVariable("id") Long articleId
    ) {
        this.deleteArticleById.execute(articleId);
        return ResponseEntity.noContent().build();
    }
}
