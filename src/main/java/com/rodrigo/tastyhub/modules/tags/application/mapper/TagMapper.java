package com.rodrigo.tastyhub.modules.tags.application.mapper;

import com.rodrigo.tastyhub.modules.tags.application.dto.response.TagDto;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;

public final class TagMapper {
    private TagMapper() {}

    public static TagDto toTagDto(Tag tag) {
        return new TagDto(
            tag.getId(),
            tag.getName()
        );
    }
}
