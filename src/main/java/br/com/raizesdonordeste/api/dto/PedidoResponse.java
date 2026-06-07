package br.com.raizesdonordeste.api.dto;

import br.com.raizesdonordeste.api.model.Pedido;
import br.com.raizesdonordeste.api.model.enums.StatusPedido;

import java.math.BigDecimal;

public class PedidoResponse {

    public Long id;
    public String cliente;
    public String unidade;
    public StatusPedido statusPedido;
    public BigDecimal valorTotal;
    public BigDecimal valorFinal;

    public PedidoResponse(Pedido pedido) {
        this.id = pedido.getId();
        this.cliente = pedido.getCliente().getNome();
        this.unidade = pedido.getUnidade().getNome();
        this.statusPedido = pedido.getStatusPedido();
        this.valorTotal = pedido.getValorTotal();
        this.valorFinal = pedido.getValorTotal();
    }

    public PedidoResponse(Pedido pedido, BigDecimal valorFinal) {
        this.id = pedido.getId();
        this.cliente = pedido.getCliente().getNome();
        this.unidade = pedido.getUnidade().getNome();
        this.statusPedido = pedido.getStatusPedido();
        this.valorTotal = pedido.getValorTotal();
        this.valorFinal = valorFinal;
    }
}