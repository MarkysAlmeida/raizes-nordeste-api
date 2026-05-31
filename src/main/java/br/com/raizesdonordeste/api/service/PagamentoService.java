package br.com.raizesdonordeste.api.service;

import br.com.raizesdonordeste.api.dto.PagamentoRequest;
import br.com.raizesdonordeste.api.model.Pagamento;
import br.com.raizesdonordeste.api.model.Pedido;
import br.com.raizesdonordeste.api.model.Usuario;
import br.com.raizesdonordeste.api.model.enums.StatusPagamento;
import br.com.raizesdonordeste.api.model.enums.StatusPedido;
import br.com.raizesdonordeste.api.repository.PagamentoRepository;
import br.com.raizesdonordeste.api.repository.PedidoRepository;
import br.com.raizesdonordeste.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public PagamentoService(
            PagamentoRepository pagamentoRepository,
            PedidoRepository pedidoRepository,
            UsuarioRepository usuarioRepository) {

        this.pagamentoRepository = pagamentoRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Pagamento processar(Long pedidoId, PagamentoRequest request) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setFormaPagamento(request.formaPagamento);
        pagamento.setValor(pedido.getValorTotal());

        boolean aprovado = true;

        if (aprovado) {
            pagamento.setStatusPagamento(StatusPagamento.APROVADO);
            pedido.setStatusPedido(StatusPedido.PAGO);

            Usuario cliente = usuarioRepository.findById(pedido.getCliente().getId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            int pontosAtuais = cliente.getPontosFidelidade() == null ? 0 : cliente.getPontosFidelidade();
            int pontosGanhos = pedido.getValorTotal().intValue();

            cliente.setPontosFidelidade(pontosAtuais + pontosGanhos);

            usuarioRepository.save(cliente);

        } else {
            pagamento.setStatusPagamento(StatusPagamento.RECUSADO);
            pedido.setStatusPedido(StatusPedido.CANCELADO);
        }

        pedidoRepository.save(pedido);

        return pagamentoRepository.save(pagamento);
    }
}