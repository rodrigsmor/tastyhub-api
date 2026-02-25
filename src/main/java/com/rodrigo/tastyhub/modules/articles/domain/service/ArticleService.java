package com.rodrigo.tastyhub.modules.articles.domain.service;

import com.rodrigo.tastyhub.modules.articles.domain.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    public Long getArticlesCountByUserId(Long userId) {
        return this.articleRepository.countByUserId(userId);
    }
}
