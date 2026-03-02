package com.rodrigo.tastyhub.modules.articles.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "article_statistics")
public class ArticleStatistics {
    @Id
    private Long recipeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount = 0;

    @Column(name = "favorites_count", nullable = false)
    private Integer favoritesCount = 0;

    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;
}
