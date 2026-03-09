package com.rodrigo.tastyhub.modules.recipes.domain.service;

import com.rodrigo.tastyhub.modules.recipes.domain.model.Currency;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.CurrencyRepository;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class CurrencyServiceTest {
    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private CurrencyService currencyService;

    @Nested
    @DisplayName("Tests for Find By Id Method")
    class FindByIdTests {
        @Test
        @DisplayName("Should return the currency corresponding to the specified ID")
        void shouldReturnTheCurrencyCorrespondingToSpecifiedId() {
            Short currencyId = 1;

            Currency currency = new Currency();

            currency.setId(currencyId);
            currency.setName("American Dollar");
            currency.setCode("USD");
            currency.setSymbol("$");

            when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(currency));

            Currency response = currencyService.findById(currencyId);

            assertEquals(currency, response);
            verify(currencyRepository, times(1)).findById(eq(currencyId));
        }

        @Test
        @DisplayName("Should throws DomainException when Currency is not found")
        void shouldThrowsDomainExceptionWhenCurrencyIsNotFound() {
            Short currencyId = 1;

            when(currencyRepository.findById(currencyId)).thenReturn(Optional.empty());

            assertThrows(DomainException.class, () -> currencyService.findById(currencyId));
        }
    }
}