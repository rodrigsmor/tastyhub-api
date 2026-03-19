package com.rodrigo.tastyhub.modules.tags.domain.service;

import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.repository.TagRepository;
import com.rodrigo.tastyhub.modules.user.domain.repository.UserRepository;
import com.rodrigo.tastyhub.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagService {
    @Autowired
    private TagRepository tagRepository;

    public List<Tag> findAllById(Collection<Long> tagIds) {
        return tagRepository.findAllById(tagIds);
    }

    public List<Tag> syncAll(Collection<Long> tagIds) {
        List<Tag> tags = this.findAllById(tagIds);

        if (tags.size() != tagIds.size()) {
            throw new ResourceNotFoundException("One or more provided Tags do not exist.");
        }

        return tags;
    }

    @Transactional
    public Set<Tag> ensureTagsExist(Set<String> names) {
        if (names == null || names.isEmpty()) return Collections.emptySet();

        List<Tag> existingTags = tagRepository.findByNameIn(names);
        Set<String> existingNames = existingTags.stream()
            .map(Tag::getName)
            .collect(Collectors.toSet());

        List<Tag> newTags = names.stream()
            .filter(name -> !existingNames.contains(name))
            .map(name -> {
                Tag t = new Tag();
                t.setName(name);
                return t;
            })
            .toList();

        if (!newTags.isEmpty()) {
            existingTags.addAll(tagRepository.saveAll(newTags));
        }

        return new HashSet<>(existingTags);
    }
}
