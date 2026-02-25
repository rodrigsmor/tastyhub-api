package com.rodrigo.tastyhub.modules.recipes.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "preparation_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreparationStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String instruction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;
}
