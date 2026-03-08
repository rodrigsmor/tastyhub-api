package com.rodrigo.tastyhub.modules.comments.application.dto.response;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReviewResponseDto(
    @Schema(description = "Unique identifier of the review", example = "101")
    Long id,

    @Schema(description = "Summary of the user who wrote the review")
    UserSummaryDto author,

    @Schema(description = "The review text", example = "This carbonara recipe is amazing!")
    String content,

    @Schema(description = "The rating given", example = "4.5")
    BigDecimal rating,

    @Schema(description = "Creation timestamp in ISO 8601 format", example = "2026-03-07T14:15:30Z")
    OffsetDateTime createdAt
) {}
