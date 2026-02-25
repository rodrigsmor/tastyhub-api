package com.rodrigo.tastyhub.modules.recipes.application.mapper;

import com.rodrigo.tastyhub.modules.recipes.application.dto.response.PreparationStepDto;
import com.rodrigo.tastyhub.modules.recipes.domain.model.PreparationStep;

public final class PreparationStepMapper {
    private PreparationStepMapper() {}

    public static PreparationStepDto toPreparationStepDto(PreparationStep step) {
        return new PreparationStepDto(
            step.getId(),
            step.getStepNumber(),
            step.getInstruction()
        );
    }
}
