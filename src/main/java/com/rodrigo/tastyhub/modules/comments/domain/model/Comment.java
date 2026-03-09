package com.rodrigo.tastyhub.modules.comments.domain.model;

import com.rodrigo.tastyhub.modules.articles.domain.model.Article;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    public static Comment createReview(BigDecimal rating, String content, User author, Recipe recipe) {
        validateRating(rating);
        validateContent(content);

        Comment comment = new Comment();

        comment.rating = rating;
        comment.content = content;
        comment.user = author;
        comment.recipe = recipe;
        comment.createdAt = OffsetDateTime.now();

        recipe.updateStatisticRating(rating);

        return comment;
    }

    private static void validateRating(BigDecimal rating) {
        if (rating == null || rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(new BigDecimal("5")) > 0) {
            throw new DomainException("Rating must be between 0 and 5");
        }
    }

    private static void validateContent(String content) {
        if (content == null || content.trim().length() < 10) {
            throw new DomainException("Review content must be at least 10 characters long");
        }
    }

    public void addUser(User user) {
        this.user = user;
    }
}
