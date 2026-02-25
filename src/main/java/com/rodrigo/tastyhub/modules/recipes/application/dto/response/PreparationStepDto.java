package com.rodrigo.tastyhub.modules.recipes.application.dto.response;

public record PreparationStepDto(
    Long id,
    Integer stepNumber,
    String instruction
) {}
