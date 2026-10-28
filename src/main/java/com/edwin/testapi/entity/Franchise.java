package com.edwin.testapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "franchises")
@Getter
@Setter
@NoArgsConstructor
public class Franchise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la franquicia es obligatorio")
    @Column(nullable = false)
    private String name;

    @OneToMany(
            mappedBy = "franchise",
            cascade = CascadeType.PERSIST
    )
    private List<Branch> branches = new ArrayList<>();
}


