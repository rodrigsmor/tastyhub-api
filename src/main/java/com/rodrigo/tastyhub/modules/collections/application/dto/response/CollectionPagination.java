package com.rodrigo.tastyhub.modules.collections.application.dto.response;

import com.rodrigo.tastyhub.shared.dto.response.PaginationMetadata;

import java.util.List;

public record CollectionPagination(
    List<UserCollectionResponseDto> collections,
    PaginationMetadata metadata
) {}
