package com.rodrigo.tastyhub.modules.articles.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to create a new article")
public record CreateArticleDto(
    @Schema(description = "Title of the Article", example = "10 Essential Spices Every Home Cook Should Own")
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100)
    String title,

    @Schema(
        description = "The content of the Article",
        example = "Cooking is a big challenge, but there are some best practices that can help you achieve better results. A tasty, appealing, and mouth-watering meal requires preparation, patience, and practice. Check out my thoughts below:"
    )
    @NotNull(message = "Article must have a content!")
    String content,

    @Schema(example = "true", description = "Visibility of article")
    Boolean isPublic,

    @Schema(description = "ISO 639-1 language code with region", example = "en-US")
    @NotBlank(message = "Language is required")
    String language
) {}
