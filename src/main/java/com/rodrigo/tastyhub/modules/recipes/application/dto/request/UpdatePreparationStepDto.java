package com.rodrigo.tastyhub.modules.recipes.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePreparationStepDto(
    @Schema(example = "1", description = "The ID of the Preparation Step")
    Long id,

    @Schema(example = "1", description = "The order of this step")
    @NotNull(message = "Step number is required")
    @Min(value = 1, message = "Step number must be at least 1")
    Integer stepNumber,

    @Schema(example = "Preheat the oven to 180°C", description = "Instruction text")
    @NotBlank(message = "Instruction text cannot be empty")
    @Size(max = 1000, message = "Instruction is too long")
    String instruction
) {}
