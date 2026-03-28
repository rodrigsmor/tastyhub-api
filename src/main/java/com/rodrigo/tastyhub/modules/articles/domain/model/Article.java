package com.rodrigo.tastyhub.modules.articles.domain.model;

import com.rodrigo.tastyhub.modules.comments.domain.model.Comment;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "cover_alt", length = 500)
    private String coverAlt;

    @Column(name = "is_public", length = 500)
    private boolean isPublic = true;

    @Column(name = "language", nullable = false, length = 5)
    private String language = "en-US";

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToOne(mappedBy = "article", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn
    private ArticleStatistics statistics;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Article(
        String title,
        String content,
        User author,
        Boolean isPublic,
        String language
    ) {
        init();

        this.isPublic = isPublic != null ? isPublic : true;
        this.title = Objects.requireNonNull(title, "Title is required");
        this.author = Objects.requireNonNull(author, "Author is required");
        this.language = Objects.requireNonNull(language, "Language is required");
        this.content = Objects.requireNonNull(content, "Article must have a content!");
    }

    public void init() {
        this.statistics = new ArticleStatistics();
        this.statistics.setArticle(this);
        this.comments = new ArrayList<>();
    }

    public void update(
        String title,
        String content,
        Boolean isPublic,
        String language,
        Long userId
    ) {
        this.validateOwnership(userId);

        this.validateNotBlank(title, "Title");
        this.validateNotBlank(content, "Content");
        this.validateNotBlank(language, "Language");

        if (title != null) this.title = title.trim();
        if (title != null) this.content = content.trim();
        if (title != null) this.language = language.trim();
        if (title != null) this.isPublic = isPublic;
    }

    public void validateOwnership(Long currentUserId) {
        if (this.author == null || !this.author.getId().equals(currentUserId)) {
            throw new ForbiddenException("You are not the author of this recipe and cannot modify it.");
        }
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value != null && value.trim().isBlank()) {
            throw new DomainException(fieldName + " cannot be empty");
        }
    }
}
