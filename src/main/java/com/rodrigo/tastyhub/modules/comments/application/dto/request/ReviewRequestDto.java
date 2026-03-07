package com.rodrigo.tastyhub.modules.comments.application.dto.request;

import java.math.BigDecimal;

public record ReviewRequestDto(
    String content,
    Long authorId,
    BigDecimal rating
) {}
