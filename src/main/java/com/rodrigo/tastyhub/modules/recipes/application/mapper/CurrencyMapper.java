package com.rodrigo.tastyhub.modules.recipes.application.mapper;

import com.rodrigo.tastyhub.modules.recipes.application.dto.response.RecipeCurrencyDto;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Currency;

public final class CurrencyMapper {
    private CurrencyMapper() {}

    public static RecipeCurrencyDto toRecipeCurrencyDto(Currency currency) {
        return new RecipeCurrencyDto(
            currency.getId(),
            currency.getCode(),
            currency.getName(),
            currency.getSymbol()
        );
    }
}
