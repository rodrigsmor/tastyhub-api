package com.rodrigo.tastyhub.modules.comments.domain.services;

import com.rodrigo.tastyhub.modules.comments.application.dto.request.ReviewRequestDto;
import com.rodrigo.tastyhub.modules.comments.application.dto.response.ReviewPagination;
import com.rodrigo.tastyhub.modules.comments.application.dto.response.ReviewResponseDto;
import com.rodrigo.tastyhub.modules.comments.application.dto.response.ReviewStarOverview;
import com.rodrigo.tastyhub.modules.comments.application.dto.response.ReviewSummaryDto;
import com.rodrigo.tastyhub.modules.comments.application.mapper.CommentMapper;
import com.rodrigo.tastyhub.modules.comments.domain.model.Comment;
import com.rodrigo.tastyhub.modules.comments.domain.model.CommentSortBy;
import com.rodrigo.tastyhub.modules.comments.domain.model.ReviewStatsProjection;
import com.rodrigo.tastyhub.modules.comments.domain.repository.CommentRepository;
import com.rodrigo.tastyhub.modules.comments.infrastructure.persistence.CommentSpecification;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.shared.kernel.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;
import com.rodrigo.tastyhub.shared.enums.SortDirection;
import com.rodrigo.tastyhub.shared.exception.DomainException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final RecipeService recipeService;
    private final CommentRepository commentRepository;
    private final SecurityService securityService;

    @RequiresVerification
    public Comment reviewRecipeById(Long recipeId, ReviewRequestDto reviewDto) {
        User author = securityService.getCurrentUser();
        Recipe recipe = recipeService.findByIdOrThrow(recipeId);

        Comment comment = Comment.createReview(
            reviewDto.rating(),
            reviewDto.content(),
            author,
            recipe
        );

        return commentRepository.save(comment);
    }

    public ReviewPagination listReviewsByRecipeId(
        Long recipeId,
        Integer pageNumber,
        Integer size,
        CommentSortBy sortBy,
        SortDirection direction
    ) {
        if (recipeId == null || recipeId < 0)
            throw new DomainException("Recipe ID is required");

        Pageable pageable = PageRequest.of(
            pageNumber,
            size,
            buildSort(sortBy, direction)
        );

        Page<Comment> page = commentRepository.findAll(
            CommentSpecification.withFilters(recipeId),
            pageable
        );

        List<ReviewResponseDto> reviews = page.getContent()
            .stream()
            .map(CommentMapper::toReview)
            .toList();

        PaginationMetadata metadata = new PaginationMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            direction,
            page.hasNext(),
            page.hasPrevious()
        );

        ReviewSummaryDto summaryDto = buildReviewSummary(recipeId);

        return new ReviewPagination(reviews, summaryDto, metadata);
    }

    @Transactional
    public ReviewSummaryDto buildReviewSummary(Long recipeId) {
        ReviewStatsProjection stats = commentRepository.getReviewStatsByRecipeId(recipeId);

        if (stats == null || stats.getTotalReviews() == 0) {
            return new ReviewSummaryDto(0, 0, 0.0, new ArrayList<>());
        }

        List<Map<String, Object>> breakdownRaw = commentRepository.getRatingCountBreakdown(recipeId);

        List<ReviewStarOverview> overviews = mapToStarOverview(breakdownRaw, stats.getTotalReviews());

        return new ReviewSummaryDto(
            stats.getTotalUsers(),
            stats.getTotalReviews(),
            stats.getAverageRating() != null ? stats.getAverageRating() : 0.0,
            overviews
        );
    }

    private List<ReviewStarOverview> mapToStarOverview(List<Map<String, Object>> raw, int total) {
        Map<Integer, Integer> counts = new HashMap<>();
        IntStream.rangeClosed(1, 5).forEach(i -> counts.put(i, 0));

        for (Map<String, Object> row : raw) {
            double rawRating = ((Number) row.get("ratingValue")).doubleValue();
            int stars = (int) rawRating;

            if (stars >= 1 && stars <= 5) {
                counts.put(stars, counts.get(stars) + ((Number) row.get("count")).intValue());
            } else if (stars < 1 && counts.containsKey(1)) {
                counts.put(1, counts.get(1) + ((Number) row.get("count")).intValue());
            }
        }

        return counts.entrySet().stream()
            .sorted(Map.Entry.<Integer, Integer>comparingByKey().reversed())
            .map(entry -> {
                double percentage = (total > 0) ? (entry.getValue() * 100.0) / total : 0.0;
                return new ReviewStarOverview(entry.getKey(), percentage, entry.getValue());
            })
            .toList();
    }

    @Transactional
    private Sort buildSort(CommentSortBy sortBy, SortDirection direction) {
        String field = sortBy == CommentSortBy.CREATED_AT ? "createdAt" : "rating";

        return direction == SortDirection.ASC
            ? Sort.by(field).ascending()
            : Sort.by(field).descending();
    }
}
