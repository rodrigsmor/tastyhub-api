package com.rodrigo.tastyhub.modules.comments.domain;

import com.rodrigo.tastyhub.modules.comments.application.dto.request.ReviewRequestDto;
import com.rodrigo.tastyhub.modules.comments.domain.model.Comment;
import com.rodrigo.tastyhub.modules.comments.domain.repository.CommentRepository;
import com.rodrigo.tastyhub.modules.recipes.domain.model.Recipe;
import com.rodrigo.tastyhub.modules.recipes.domain.service.RecipeService;
import com.rodrigo.tastyhub.modules.user.domain.annotations.RequiresVerification;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.shared.config.security.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
