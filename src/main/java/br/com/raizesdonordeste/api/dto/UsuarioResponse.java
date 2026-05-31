package br.com.raizesdonordeste.api.dto;

import br.com.raizesdonordeste.api.model.enums.Role;

public class UsuarioResponse {

    public Long id;
    public String nome;
    public String email;
    public Role role;
    public Integer pontosFidelidade;

    public UsuarioResponse(Long id, String nome, String email, Role role, Integer pontosFidelidade) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.pontosFidelidade = pontosFidelidade;
    }
}