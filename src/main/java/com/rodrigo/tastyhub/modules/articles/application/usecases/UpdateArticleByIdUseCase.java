package com.rodrigo.tastyhub.modules.articles.application.usecases;

import com.rodrigo.tastyhub.modules.articles.application.dto.request.UpdateArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.dto.response.FullArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.mapper.ArticleMapper;
import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.articles.domain.service.ArticleService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateArticleByIdUseCase {
    private final ArticleService articleService;
    private final SecurityService securityService;

    public FullArticleDto execute(Long articleId, UpdateArticleDto newData) {
        User user = this.securityService.getCurrentUser();

        Article article = articleService.update(
            articleId,
            newData.title(),
            newData.content(),
            newData.isPublic(),
            newData.language(),
            user.getId()
        );

        return ArticleMapper.toFull(article);
    }
}
