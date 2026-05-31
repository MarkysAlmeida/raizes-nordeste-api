package br.com.raizesdonordeste.api.model;

import br.com.raizesdonordeste.api.model.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    private String senha;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Integer pontosFidelidade = 0;
}