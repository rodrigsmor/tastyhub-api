package com.rodrigo.tastyhub.modules.recipes.domain.repository;

import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    long countByUserId(Long userId);
}
