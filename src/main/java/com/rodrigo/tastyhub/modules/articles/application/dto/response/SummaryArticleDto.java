package com.rodrigo.tastyhub.modules.articles.application.dto.response;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary representation of an articles for listings and search results")
public record SummaryArticleDto(
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

    @Schema(description = "Simplified information about the article author")
    UserSummaryDto author,

    @Schema(description = "Total number of comments", example = "24")
    long commentsCount,

    @Schema(description = "Total number of likes", example = "450")
    double likesCount,

    @Schema(description = "Total number of users who favorited this article", example = "1250")
    long favoritesCount
) {}
