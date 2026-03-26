package com.rodrigo.tastyhub.modules.articles.application.dto.response;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Detailed representation of an article, including all instructions, ingredients, and metrics")
public record FullArticleDto(
    @Schema(description = "Unique identifier of the article", example = "101")
    long id,

    @Schema(description = "Title of the Article",
        example = "10 Essential Spices Every Home Cook Should Own")
    String title,

    @Schema(description = "URL of the article's main cover image",
        example = "https://cdn.tastyhub.com/images/articles/essential-spices.jpg")
    String coverUrl,

    @Schema(description = "Alternative text for the cover image (accessibility)",
        example = "A top-down view of various colorful spices in small wooden bowls")
    String coverAlt,

    @Schema(description = "The content of the Article", example = "Cooking is a big challenge, but there are some best practices that can help you achieve better results. A tasty, appealing, and mouth-watering meal requires preparation, patience, and practice. Check out my thoughts below:")
    String content,

    @Schema(description = "Simplified information about the article author")
    UserSummaryDto author,

    @Schema(description = "Total number of comments", example = "24")
    long commentsCount,

    @Schema(description = "Total number of likes", example = "450")
    double likesCount,

    @Schema(description = "Total number of users who favorited this article", example = "1250")
    long favoritesCount,

    @Schema(description = "Timestamp when the recipe was first published", example = "2026-03-01T20:00:00Z")
    OffsetDateTime createdAt,

    @Schema(description = "Timestamp of the last modification to the recipe", example = "2026-03-01T21:30:00Z")
    OffsetDateTime updatedAt
) {}
