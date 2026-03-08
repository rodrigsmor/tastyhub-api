package com.rodrigo.tastyhub.modules.comments.application.mapper;

import com.rodrigo.tastyhub.modules.comments.application.dto.response.ReviewResponseDto;
import com.rodrigo.tastyhub.modules.comments.domain.model.Comment;
import com.rodrigo.tastyhub.modules.user.application.mapper.UserMapper;
import org.springframework.stereotype.Component;

@Component
public final class CommentMapper {
    public static ReviewResponseDto toReview(Comment comment) {
        return new ReviewResponseDto(
            comment.getId(),
            UserMapper.toSummary(comment.getUser()),
            comment.getContent(),
            comment.getRating(),
            comment.getCreatedAt()
        );
    }
}
