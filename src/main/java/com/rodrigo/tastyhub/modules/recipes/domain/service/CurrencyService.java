package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.domain.model.Currency;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyRepository currencyRepository;

    public Currency findById(Short id) throws BadRequestException {
        return currencyRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("The currency specified does not exist!"));
    }
}
