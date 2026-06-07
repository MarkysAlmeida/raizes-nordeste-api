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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PagamentoService {

    private static final Logger logger =
            LoggerFactory.getLogger(PagamentoService.class);

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

        if (pedido.getStatusPedido() == StatusPedido.PAGO ||
                pedido.getStatusPedido() == StatusPedido.EM_PREPARO ||
                pedido.getStatusPedido() == StatusPedido.SAIU_PARA_ENTREGA) {
            throw new RuntimeException("Pedido já foi pago");
        }

        if (pedido.getStatusPedido() == StatusPedido.CANCELADO) {
            throw new RuntimeException("Pedido cancelado não pode ser pago");
        }

        if (pedido.getStatusPedido() == StatusPedido.ENTREGUE) {
            throw new RuntimeException("Pedido entregue não pode ser pago novamente");
        }

        Usuario cliente = usuarioRepository.findById(pedido.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setFormaPagamento(request.formaPagamento);

        BigDecimal valorFinal = pedido.getValorTotal();

        boolean aprovado = true;

        if (aprovado) {

            if (Boolean.TRUE.equals(request.usarPontos)) {

                int pontosAtuais = cliente.getPontosFidelidade() == null ? 0 : cliente.getPontosFidelidade();

                BigDecimal desconto = BigDecimal.valueOf(pontosAtuais)
                        .divide(BigDecimal.TEN);

                if (desconto.compareTo(valorFinal) > 0) {
                    desconto = valorFinal;
                }

                int pontosUsados = desconto
                        .multiply(BigDecimal.TEN)
                        .intValue();

                valorFinal = valorFinal.subtract(desconto);

                cliente.setPontosFidelidade(pontosAtuais - pontosUsados);

                usuarioRepository.save(cliente);

                logger.info(
                        "Pontos utilizados - Cliente: {} Pontos usados: {} Desconto: {}",
                        cliente.getEmail(),
                        pontosUsados,
                        desconto
                );
            }

            pagamento.setStatusPagamento(StatusPagamento.APROVADO);
            pagamento.setValor(valorFinal);

            pedido.setStatusPedido(StatusPedido.EM_PREPARO);

            logger.info(
                    "Pagamento aprovado - Pedido: {} Cliente: {} Valor final: {}",
                    pedido.getId(),
                    cliente.getEmail(),
                    valorFinal
            );

        } else {
            pagamento.setStatusPagamento(StatusPagamento.RECUSADO);
            pagamento.setValor(pedido.getValorTotal());
            pedido.setStatusPedido(StatusPedido.CANCELADO);

            logger.warn(
                    "Pagamento recusado - Pedido: {} Valor: {}",
                    pedido.getId(),
                    pedido.getValorTotal()
            );
        }

        pedidoRepository.save(pedido);

        return pagamentoRepository.save(pagamento);
    }
}