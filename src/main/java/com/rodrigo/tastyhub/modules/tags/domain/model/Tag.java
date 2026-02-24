package com.rodrigo.tastyhub.modules.tags.domain.model;

import com.rodrigo.tastyhub.modules.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        unique = true,
        nullable = false,
        length = 20
    )
    private String name;

    @Builder.Default
    @ManyToMany(mappedBy = "followedTags")
    private Set<User> followers = new HashSet<>();

    public Tag(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
