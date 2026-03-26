package com.rodrigo.tastyhub.modules.articles.application.mapper;

import com.rodrigo.tastyhub.modules.articles.application.dto.response.FullArticleDto;
import com.rodrigo.tastyhub.modules.articles.application.dto.response.SummaryArticleDto;
import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.user.application.mapper.UserMapper;
import com.rodrigo.tastyhub.shared.config.storage.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArticleMapper {
    private static ImageStorageService storageService;

    @Autowired
    public void setStorageService(ImageStorageService storageService) {
        ArticleMapper.storageService = storageService;
    }

    public static SummaryArticleDto toSummary(Article article) {
        return new SummaryArticleDto(
            article.getId(),
            article.getTitle(),
            storageService.generateImageUrl(article.getCoverUrl()),
            article.getCoverAlt(),
            UserMapper.toSummary(article.getAuthor()),
            article.getStatistics().getCommentsCount(),
            article.getStatistics().getLikesCount(),
            article.getStatistics().getFavoritesCount()
        );
    }

    public static FullArticleDto toFull(Article article) {
        return new FullArticleDto(
            article.getId(),
            article.getTitle(),
            storageService.generateImageUrl(article.getCoverUrl()),
            article.getCoverAlt(),
            article.getContent(),
            UserMapper.toSummary(article.getAuthor()),
            article.getStatistics().getCommentsCount(),
            article.getStatistics().getLikesCount(),
            article.getStatistics().getFavoritesCount(),
            article.getCreatedAt(),
            article.getUpdatedAt()
        );
    }
}
