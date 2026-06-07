package br.com.raizesdonordeste.api.dto;

import java.math.BigDecimal;

public class ProdutoUpdateRequest {

    public String nome;
    public String descricao;
    public BigDecimal preco;
    public Integer quantidade;
    public Boolean ativo;
}