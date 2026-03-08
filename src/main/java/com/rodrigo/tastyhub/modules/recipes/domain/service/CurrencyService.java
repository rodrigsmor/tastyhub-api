package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.domain.model.Currency;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.CurrencyRepository;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyRepository currencyRepository;

    public Currency findById(Short id) {
        return currencyRepository.findById(id)
            .orElseThrow(() -> new DomainException("The currency specified does not exist!"));
    }
}
