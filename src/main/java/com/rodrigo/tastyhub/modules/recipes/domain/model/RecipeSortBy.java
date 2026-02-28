package com.rodrigo.tastyhub.modules.recipes.domain.model;

public enum RecipeSortBy {
    RELEVANCE, // mais curtidas, comentários, salvamentos
    REVIEWS, // avaliar pelos que possuem melhor avaliacao, depois pelos que possuem MAIS avaliacoes positivas
    CREATION_DATE,
    TITLE,
    INGREDIENTS,
}
