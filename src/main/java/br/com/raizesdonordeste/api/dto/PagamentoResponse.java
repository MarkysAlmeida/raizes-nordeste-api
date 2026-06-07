package br.com.raizesdonordeste.api.dto;

import br.com.raizesdonordeste.api.model.Pagamento;
import br.com.raizesdonordeste.api.model.enums.StatusPagamento;

import java.math.BigDecimal;

public class PagamentoResponse {

    public Long id;
    public Long pedidoId;
    public StatusPagamento statusPagamento;
    public BigDecimal valor;

    public PagamentoResponse(Pagamento pagamento) {
        this.id = pagamento.getId();
        this.pedidoId = pagamento.getPedido().getId();
        this.statusPagamento = pagamento.getStatusPagamento();
        this.valor = pagamento.getValor();
    }
}