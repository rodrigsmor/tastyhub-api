package com.rodrigo.tastyhub.modules.recipes.application.dto.response;

public record RecipeCurrencyDto(
    Short id,
    String code,
    String name,
    String symbol
) {}
