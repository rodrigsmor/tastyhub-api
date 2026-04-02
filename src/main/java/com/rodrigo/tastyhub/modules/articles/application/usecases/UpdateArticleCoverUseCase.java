package com.rodrigo.tastyhub.modules.articles.application.usecases;

import com.rodrigo.tastyhub.modules.articles.application.dto.response.FullArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.mapper.ArticleMapper;
import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.articles.domain.service.ArticleService;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import com.rodrigo.tastyhub.shared.kernel.annotations.FileCleanup;
import com.rodrigo.tastyhub.shared.kernel.annotations.RequiresVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UpdateArticleCoverUseCase {
    private final ArticleService articleService;
    private final SecurityService securityService;
    private final ImageStorageService imageStorageService;

    @RequiresVerification
    @FileCleanup
    @Transactional
    public FullArticleDto execute(Long recipeId, MultipartFile file, String newAlternativeText) {
        User user = securityService.getCurrentUser();
        Article article = articleService.findByIdOrThrow(recipeId);

        String oldFileName = article.getCoverUrl();
        String newFileName = imageStorageService.storeImage(file);

        Article updatedArticle = articleService.updateCover(
            recipeId,
            newFileName,
            newAlternativeText,
            user.getId()
        );

        if (oldFileName != null) {
            imageStorageService.deleteImage(oldFileName);
        }

        return ArticleMapper.toFull(updatedArticle);
    }
}
