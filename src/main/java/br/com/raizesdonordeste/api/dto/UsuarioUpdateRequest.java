package br.com.raizesdonordeste.api.dto;

import br.com.raizesdonordeste.api.model.enums.Role;

public class UsuarioUpdateRequest {

    public String nome;
    public String email;
    public String senha;
    public Role role;
    public Long unidadeId;
    public Boolean ativo;
}