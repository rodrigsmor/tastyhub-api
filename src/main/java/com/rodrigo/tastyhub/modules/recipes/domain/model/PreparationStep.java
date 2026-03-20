package com.rodrigo.tastyhub.modules.recipes.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

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

    public PreparationStep(Integer stepNumber, String instruction) {
        this.stepNumber = Objects.requireNonNull(stepNumber, "Step Number is required");
        this.instruction = Objects.requireNonNull(instruction, "Instruction is required");
    }
}
