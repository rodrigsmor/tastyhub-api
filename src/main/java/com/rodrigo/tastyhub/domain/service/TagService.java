package com.rodrigo.tastyhub.domain.service;

import com.rodrigo.tastyhub.domain.model.Tag;
import com.rodrigo.tastyhub.domain.repository.TagRepository;
import com.rodrigo.tastyhub.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

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
