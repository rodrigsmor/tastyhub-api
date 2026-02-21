package com.rodrigo.tastyhub.domain.repository;

import com.rodrigo.tastyhub.domain.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Boolean existsByName(String name);
    Optional<Tag> findByName(String name);
    List<Tag> findByNameIn(Collection<String> name);
}
