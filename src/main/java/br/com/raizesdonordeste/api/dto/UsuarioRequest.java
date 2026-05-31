package br.com.raizesdonordeste.api.dto;

import br.com.raizesdonordeste.api.model.enums.Role;

public class UsuarioRequest {

    public String nome;
    public String email;
    public String senha;
    public Role role;
}