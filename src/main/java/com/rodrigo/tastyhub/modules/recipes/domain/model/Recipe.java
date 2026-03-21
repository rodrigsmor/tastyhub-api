package com.rodrigo.tastyhub.modules.recipes.domain.model;

import com.rodrigo.tastyhub.modules.comments.domain.model.Comment;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import com.rodrigo.tastyhub.shared.exception.ForbiddenException;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "cook_time_min")
    private Integer cookTimeMin;

    @Column(name = "cook_time_max")
    private Integer cookTimeMax;

    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private RecipeCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @Column(name = "cover_alt", length = 500)
    private String coverAlt;

    @Builder.Default
    @Column(name = "is_public", length = 500)
    private boolean isPublic = true;

    @OneToOne(mappedBy = "recipe", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn
    private RecipeStatistics statistics;

    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<PreparationStep> steps = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeMedia> media = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "recipe_tags",
        joinColumns = @JoinColumn(name = "recipe_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public Recipe(
        String title,
        String description,
        User author,
        RecipeCategory category,
        Integer cookTimeMin,
        Integer cookTimeMax,
        BigDecimal estimatedCost,
        Currency currency,
        List<Tag> tags,
        List<PreparationStep> steps,
        List<RecipeIngredient> ingredients
    ) {
        init();

        this.title = Objects.requireNonNull(title, "Title is required");
        this.description = Objects.requireNonNull(description, "Description is required");
        this.author = Objects.requireNonNull(author, "Author is required");
        this.category = Objects.requireNonNull(category, "Category is required");

        this.tags = (tags != null) ? new HashSet<>(tags) : new HashSet<>();

        steps.forEach(this::addStep);

        ingredients.forEach(ingredient -> this.addIngredient(
            ingredient.getIngredient(),
            ingredient.getQuantity(),
            ingredient.getUnit()
        ));

        this.updateTiming(cookTimeMin, cookTimeMax);
        this.updateMonetaryDetails(estimatedCost, currency);
    }

    public void init() {
        this.statistics = new RecipeStatistics();
        this.statistics.setRecipe(this);
        this.comments = new ArrayList<>();
        this.media = new ArrayList<>();
    }

    public void validateOwnership(Long currentUserId) {
        if (this.author == null || !this.author.getId().equals(currentUserId)) {
            throw new ForbiddenException("You are not the author of this recipe and cannot modify it.");
        }
    }

    public void update(
        String title,
        String description,
        RecipeCategory category,
        Integer cookTimeMin,
        Integer cookTimeMax,
        BigDecimal estimatedCost,
        Currency currency,
        List<Tag> tags
    ) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (category != null) this.category = category;
        if (tags != null) this.updateAllTags(tags);

        this.updateMonetaryDetails(estimatedCost, currency);
        this.updateTiming(cookTimeMin, cookTimeMax);
    }

    public void updateAllTags(List<Tag> tags) {
        this.getTags().clear();

        this.getTags().addAll(tags);
    }

    public void updateTiming(Integer newMin, Integer newMax) {
        Integer resolvedMin = (newMin != null) ? newMin : this.cookTimeMin;
        Integer resolvedMax = (newMax != null) ? newMax : this.cookTimeMax;

        if (resolvedMin != null && resolvedMax != null && resolvedMax < resolvedMin) {
            throw new DomainException("Maximum cooking time (%d) cannot be less than minimum (%d)"
                .formatted(resolvedMax, resolvedMin));
        }

        this.cookTimeMin = newMin;
        this.cookTimeMax = newMax;
    }

    public void updateMonetaryDetails(BigDecimal newCost, Currency newCurrency) {
        if (newCost == null && newCurrency == null) {
            this.estimatedCost = null;
            this.currency = null;
            return;
        }

        if (newCost == null || newCurrency == null) {
            throw new DomainException(
                "Inconsistent monetary data: Both estimated cost and currency must be provided together."
            );
        }

        if (newCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Estimated cost cannot be negative.");
        }

        this.estimatedCost = newCost;
        this.currency = newCurrency;
    }

    public void updateCover(
        @Nullable String newCoverUrl,
        @Nullable String newCoverAlt
    ) {
        if (newCoverUrl != null) {
            this.coverAlt = null;
            this.coverUrl = null;
        }

        this.coverUrl = newCoverUrl;
        this.coverAlt = newCoverAlt;
    }

    public void addIngredient(Ingredient ingredient, BigDecimal quantity, IngredientUnitEnum unit) {
        if (this.ingredients == null) this.ingredients = new ArrayList<>();

        RecipeIngredient recipeIngredient = RecipeIngredient.builder()
            .recipe(this)
            .ingredient(ingredient)
            .quantity(quantity)
            .unit(unit)
            .build();
        this.ingredients.add(recipeIngredient);
    }

    public void addStep(PreparationStep step) {
        if (this.steps == null) this.steps = new ArrayList<>();

        this.steps.add(step);
        step.setRecipe(this);
    }

    public void addComment(User user, BigDecimal rating, String content) {
        if (rating == null || rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(new BigDecimal("5")) > 0) {
            throw new IllegalArgumentException("The rating must be between 1 and 5.");
        }

        Comment comment = Comment.builder()
            .user(user)
            .content(content)
            .rating(rating)
            .recipe(this)
            .build();

        this.comments.add(comment);

        updateStatisticRating(rating);
    }

    public void incrementFavorites() {
        if (this.statistics == null) {
            this.statistics = new RecipeStatistics();
        }
        this.statistics.incrementFavoritesCount();
    }

    public void decrementFavorites() {
        if (this.statistics == null) {
            this.statistics = new RecipeStatistics();
        }
        this.statistics.decrementFavoritesCount();
    }

    public void updateStatisticRating(BigDecimal rating) {
        if (this.statistics != null) {
            this.statistics.incrementRating(rating);
        }
    }
}
