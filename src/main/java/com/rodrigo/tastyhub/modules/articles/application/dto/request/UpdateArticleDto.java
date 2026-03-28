package com.rodrigo.tastyhub.modules.articles.application.dto.request;

import com.rodrigo.tastyhub.shared.kernel.annotations.AtLeastOneField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to update an Article")
@AtLeastOneField(message = "You must provide at least one field to update (title, content, isPublic, or language)")
public record UpdateArticleDto(
    @Schema(description = "Title of the Article", example = "10 Essential Spices Every Home Cook Should Own")
    @Nullable()
    @Size(min = 3, max = 100)
    String title,

    @Schema(
        description = "The content of the Article",
        example = "Cooking is a big challenge, but there are some best practices that can help you achieve better results. A tasty, appealing, and mouth-watering meal requires preparation, patience, and practice. Check out my thoughts below:"
    )
    @Nullable()
    String content,

    @Schema(example = "true", description = "Visibility of Article")
    Boolean isPublic,

    @Schema(description = "ISO 639-1 language code with region", example = "en-US")
    @Nullable()
    String language
) {}
