package com.rodrigo.tastyhub.modules.user.domain.model;

import com.rodrigo.tastyhub.modules.user.domain.model.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private UserRole name;

    public Long getId() {
        return id;
    }

    public UserRole getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(UserRole name) {
        this.name = name;
    }

    public Role() {}

    public Role(Long id, UserRole name) {
        this.id = id;
        this.name = name;
    }
}
