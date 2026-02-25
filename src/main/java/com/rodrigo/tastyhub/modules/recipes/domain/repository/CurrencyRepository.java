package com.rodrigo.tastyhub.modules.recipes.domain.repository;

import com.rodrigo.tastyhub.modules.recipes.domain.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Short> {
}
