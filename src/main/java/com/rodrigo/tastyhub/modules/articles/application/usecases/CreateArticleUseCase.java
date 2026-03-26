package com.rodrigo.tastyhub.modules.articles.application.usecases;

import com.rodrigo.tastyhub.modules.articles.application.dto.request.CreateArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.dto.response.FullArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.mapper.ArticleMapper;
import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.articles.domain.service.ArticleService;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateArticleUseCase {
    private final ArticleService articleService;
    private final SecurityService securityService;

    @RequiresVerification
    @Transactional
    public FullArticleDto execute(CreateArticleDto newData) {
        User user = securityService.getCurrentUser();

        Article newArticle = articleService.create(
            newData.title(),
            newData.content(),
            newData.isPublic(),
            newData.language(),
            user
        );

        return ArticleMapper.toFull(newArticle);
    }
}
