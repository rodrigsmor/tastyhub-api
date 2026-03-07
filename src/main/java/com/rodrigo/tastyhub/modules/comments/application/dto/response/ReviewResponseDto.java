package com.rodrigo.tastyhub.modules.comments.application.dto.response;

import com.rodrigo.tastyhub.modules.user.application.dto.response.UserSummaryDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReviewResponseDto(
    Long id,
    UserSummaryDto userSummaryDto,
    String content,
    BigDecimal rating,
    OffsetDateTime createdAt
) {}
