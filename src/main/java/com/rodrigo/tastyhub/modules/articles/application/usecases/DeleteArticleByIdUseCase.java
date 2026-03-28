package com.rodrigo.tastyhub.modules.articles.application.usecases;

import com.rodrigo.tastyhub.modules.articles.domain.service.ArticleService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.kernel.annotations.RequiresVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteArticleByIdUseCase {
    private final ArticleService articleService;
    private final SecurityService securityService;

    @Transactional
    @RequiresVerification
    public void execute(Long articleId) {
        User user = this.securityService.getCurrentUser();

        articleService.delete(articleId, user.getId());
    }
}
