package br.com.raizesdonordeste.api.service;

import br.com.raizesdonordeste.api.dto.PedidoRequest;
import br.com.raizesdonordeste.api.model.*;
import br.com.raizesdonordeste.api.model.enums.StatusPagamento;
import br.com.raizesdonordeste.api.model.enums.StatusPedido;
import br.com.raizesdonordeste.api.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {

    private static final Logger logger =
            LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final PagamentoRepository pagamentoRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            UsuarioRepository usuarioRepository,
            UnidadeRepository unidadeRepository,
            ProdutoRepository produtoRepository,
            EstoqueRepository estoqueRepository,
            ItemPedidoRepository itemPedidoRepository,
            PagamentoRepository pagamentoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.unidadeRepository = unidadeRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueRepository = estoqueRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    public Pedido criar(PedidoRequest request) {

        if (request.canalPedido == null) {
            throw new RuntimeException("canalPedido é obrigatório");
        }

        if (request.itens == null || request.itens.isEmpty()) {
            throw new RuntimeException("O pedido precisa ter pelo menos um item");
        }

        Usuario cliente = usuarioRepository.findById(request.clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Unidade unidade = unidadeRepository.findById(request.unidadeId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setUnidade(unidade);
        pedido.setCanalPedido(request.canalPedido);
        pedido.setStatusPedido(StatusPedido.AGUARDANDO_PAGAMENTO);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        BigDecimal valorTotal = BigDecimal.ZERO;

        for (PedidoRequest.ItemPedidoRequest itemRequest : request.itens) {

            Produto produto = produtoRepository.findById(itemRequest.produtoId)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            Estoque estoque = estoqueRepository.findByProdutoAndUnidade(produto, unidade)
                    .orElseThrow(() -> new RuntimeException("Produto sem estoque nesta unidade"));

            if (estoque.getQuantidade() < itemRequest.quantidade) {
                logger.warn("Estoque insuficiente - Produto: {} Unidade: {} Solicitado: {} Disponível: {}",
                        produto.getNome(),
                        unidade.getNome(),
                        itemRequest.quantidade,
                        estoque.getQuantidade());

                throw new RuntimeException("Estoque insuficiente");
            }

            estoque.setQuantidade(estoque.getQuantidade() - itemRequest.quantidade);
            estoqueRepository.save(estoque);

            BigDecimal subtotal = produto.getPreco()
                    .multiply(BigDecimal.valueOf(itemRequest.quantidade));

            ItemPedido item = new ItemPedido();
            item.setPedido(pedidoSalvo);
            item.setProduto(produto);
            item.setQuantidade(itemRequest.quantidade);
            item.setPrecoUnitario(produto.getPreco());
            item.setSubtotal(subtotal);

            itemPedidoRepository.save(item);

            valorTotal = valorTotal.add(subtotal);
        }

        pedidoSalvo.setValorTotal(valorTotal);

        logger.info("Pedido criado - ID: {} Cliente: {} Unidade: {} Valor: {}",
                pedidoSalvo.getId(),
                cliente.getEmail(),
                unidade.getNome(),
                valorTotal);

        return pedidoRepository.save(pedidoSalvo);
    }

    public Pedido atualizarStatus(Long pedidoId, StatusPedido novoStatus) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        StatusPedido statusAtual = pedido.getStatusPedido();

        if (statusAtual == StatusPedido.CANCELADO) {
            throw new RuntimeException("Pedido cancelado não pode ter status alterado");
        }

        if (statusAtual == StatusPedido.ENTREGUE) {
            throw new RuntimeException("Pedido entregue não pode ter status alterado");
        }

        if (novoStatus == StatusPedido.CANCELADO) {
            throw new RuntimeException("Use a rota de cancelamento para cancelar o pedido");
        }

        if (novoStatus == StatusPedido.PAGO) {
            throw new RuntimeException("Status PAGO deve ser definido pelo pagamento");
        }

        if (novoStatus == StatusPedido.EM_PREPARO) {
            throw new RuntimeException("Pedido entra em preparo automaticamente após pagamento aprovado");
        }

        if (novoStatus == StatusPedido.SAIU_PARA_ENTREGA
                && statusAtual != StatusPedido.EM_PREPARO
                && statusAtual != StatusPedido.PAGO) {

            throw new RuntimeException("Pedido só pode sair para entrega após estar em preparo");
        }

        if (novoStatus == StatusPedido.ENTREGUE && statusAtual != StatusPedido.SAIU_PARA_ENTREGA) {
            throw new RuntimeException("Pedido só pode ser entregue após sair para entrega");
        }

        pedido.setStatusPedido(novoStatus);

        if (novoStatus == StatusPedido.ENTREGUE) {

            Pagamento pagamento = pagamentoRepository.findByPedido(pedido)
                    .orElseThrow(() -> new RuntimeException("Pagamento não encontrado para este pedido"));

            if (pagamento.getStatusPagamento() != StatusPagamento.APROVADO) {
                throw new RuntimeException("Pedido só gera pontos com pagamento aprovado");
            }

            Usuario cliente = pedido.getCliente();

            int pontosAtuais = cliente.getPontosFidelidade() == null
                    ? 0
                    : cliente.getPontosFidelidade();

            int pontosGanhos = pagamento.getValor()
                    .divide(BigDecimal.TEN)
                    .intValue();

            cliente.setPontosFidelidade(pontosAtuais + pontosGanhos);

            usuarioRepository.save(cliente);

            logger.info(
                    "Pontos creditados após entrega - Pedido: {} Cliente: {} Pontos ganhos: {}",
                    pedido.getId(),
                    cliente.getEmail(),
                    pontosGanhos
            );
        }

        logger.info("Status alterado - Pedido {}: {} -> {}",
                pedido.getId(),
                statusAtual,
                novoStatus);

        return pedidoRepository.save(pedido);
    }

    public Pedido cancelar(Long pedidoId) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (pedido.getStatusPedido() == StatusPedido.ENTREGUE) {
            throw new RuntimeException("Pedido entregue não pode ser cancelado");
        }

        if (pedido.getStatusPedido() == StatusPedido.CANCELADO) {
            throw new RuntimeException("Pedido já está cancelado");
        }

        List<ItemPedido> itens = itemPedidoRepository.findAll()
                .stream()
                .filter(item -> item.getPedido().getId().equals(pedido.getId()))
                .toList();

        for (ItemPedido item : itens) {

            Estoque estoque = estoqueRepository
                    .findByProdutoAndUnidade(item.getProduto(), pedido.getUnidade())
                    .orElseThrow(() -> new RuntimeException("Estoque não encontrado para devolução"));

            estoque.setQuantidade(estoque.getQuantidade() + item.getQuantidade());

            estoqueRepository.save(estoque);
        }

        pagamentoRepository.findByPedido(pedido).ifPresent(pagamento -> {

            if (pagamento.getStatusPagamento() == StatusPagamento.APROVADO
                    && pagamento.getValor() != null
                    && pedido.getValorTotal() != null) {

                BigDecimal descontoUsado = pedido.getValorTotal().subtract(pagamento.getValor());

                if (descontoUsado.compareTo(BigDecimal.ZERO) > 0) {

                    int pontosDevolvidos = descontoUsado
                            .multiply(BigDecimal.TEN)
                            .intValue();

                    Usuario cliente = pedido.getCliente();

                    int pontosAtuais = cliente.getPontosFidelidade() == null
                            ? 0
                            : cliente.getPontosFidelidade();

                    cliente.setPontosFidelidade(pontosAtuais + pontosDevolvidos);

                    usuarioRepository.save(cliente);

                    logger.info(
                            "Pontos devolvidos por cancelamento - Pedido: {} Cliente: {} Pontos devolvidos: {}",
                            pedido.getId(),
                            cliente.getEmail(),
                            pontosDevolvidos
                    );
                }
            }
        });

        pedido.setStatusPedido(StatusPedido.CANCELADO);

        logger.info("Pedido cancelado - ID: {} Cliente: {}",
                pedido.getId(),
                pedido.getCliente().getEmail());

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarPorCliente(Long clienteId) {

        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        logger.info("Listagem de pedidos solicitada - Cliente: {}", cliente.getEmail());

        return pedidoRepository.findByCliente(cliente);
    }

    public List<Pedido> listarPorUnidade(Long unidadeId) {

        Unidade unidade = unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        logger.info("Listagem de pedidos solicitada - Unidade: {}", unidade.getNome());

        return pedidoRepository.findByUnidade(unidade);
    }
}