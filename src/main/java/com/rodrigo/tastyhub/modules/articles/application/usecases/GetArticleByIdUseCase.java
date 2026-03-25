package com.rodrigo.tastyhub.modules.articles.application.usecases;

import com.rodrigo.tastyhub.modules.articles.application.dto.response.FullArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.mapper.ArticleMapper;
import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.articles.domain.service.ArticleService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.kernel.application.CanAccessResourceVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetArticleByIdUseCase {
    private final SecurityService securityService;
    private final ArticleService articleService;
    private final CanAccessResourceVerification canAccessResource;

    public FullArticleDto execute(Long articleId) {
        Optional<User> user = securityService.getCurrentUserOptional();
        Article article = articleService.findByIdOrThrow(articleId);
        User recipeOwner = article.getAuthor();

        canAccessResource.verify(recipeOwner, user.map(User::getId).orElse(null));

        return ArticleMapper.toFull(article);
    }
}
