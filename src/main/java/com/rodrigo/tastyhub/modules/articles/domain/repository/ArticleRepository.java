package com.rodrigo.tastyhub.modules.articles.domain.repository;

import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    long countByAuthorId(Long userId);
}
