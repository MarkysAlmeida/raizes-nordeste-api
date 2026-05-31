package br.com.raizesdonordeste.api.dto;

import br.com.raizesdonordeste.api.model.enums.CanalPedido;

import java.util.List;

public class PedidoRequest {

    public Long clienteId;
    public Long unidadeId;
    public CanalPedido canalPedido;
    public List<ItemPedidoRequest> itens;

    public static class ItemPedidoRequest {
        public Long produtoId;
        public Integer quantidade;
    }
}