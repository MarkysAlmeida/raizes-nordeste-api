package br.com.raizesdonordeste.api.dto;

import br.com.raizesdonordeste.api.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioResponse {

    public Long id;
    public String nome;
    public String email;
    public Role role;
    public String unidade;
    public Integer pontosFidelidade;
    public Boolean ativo;

    public UsuarioResponse(
            Long id,
            String nome,
            String email,
            Role role,
            String unidade,
            Integer pontosFidelidade,
            Boolean ativo) {

        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.unidade = unidade;
        this.pontosFidelidade = pontosFidelidade;
        this.ativo = ativo;
    }
}