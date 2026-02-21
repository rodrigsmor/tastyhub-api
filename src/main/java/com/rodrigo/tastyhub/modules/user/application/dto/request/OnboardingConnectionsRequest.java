package com.rodrigo.tastyhub.modules.user.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = false)
public record OnboardingConnectionsRequest(
    @Schema(
        description = "IDs of users to connect with or follow",
        example = "[102, 455, 890]"
    )
    Set<Long> userIds
) {
    public boolean hasUserIds() {
        return userIds != null && !userIds.isEmpty();
    }
}