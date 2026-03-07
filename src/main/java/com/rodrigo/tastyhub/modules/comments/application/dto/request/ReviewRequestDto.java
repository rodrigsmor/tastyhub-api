package com.rodrigo.tastyhub.modules.comments.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ReviewRequestDto(
    @Schema(
        description = "Content of the review",
        example = "This carbonara recipe is amazing! Very authentic.",
        minLength = 10,
        maxLength = 1000
    )
    @NotBlank(message = "Review content cannot be empty")
    @Size(min = 10, max = 1000, message = "The review must be between 10 and 1000 characters")
    String content,

    @Schema(
        description = "Rating from 1 to 5",
        example = "4.5",
        minimum = "1",
        maximum = "5"
    )
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Minimum rating is 1")
    @DecimalMax(value = "5.0", message = "Maximum rating is 5")
    BigDecimal rating
) {}