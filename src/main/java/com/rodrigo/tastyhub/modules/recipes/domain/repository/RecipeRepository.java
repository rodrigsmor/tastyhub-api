package com.rodrigo.tastyhub.modules.recipes.domain.repository;

import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long>, JpaSpecificationExecutor<Recipe> {
    long countByAuthorId(Long userId);
}
