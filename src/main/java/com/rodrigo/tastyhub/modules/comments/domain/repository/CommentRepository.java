package com.rodrigo.tastyhub.modules.comments.domain.repository;

import com.rodrigo.tastyhub.modules.comments.domain.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
