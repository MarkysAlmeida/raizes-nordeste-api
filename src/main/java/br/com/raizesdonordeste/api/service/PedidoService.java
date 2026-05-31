package br.com.raizesdonordeste.api.service;

import br.com.raizesdonordeste.api.dto.PedidoRequest;
import br.com.raizesdonordeste.api.model.*;
import br.com.raizesdonordeste.api.model.enums.StatusPedido;
import br.com.raizesdonordeste.api.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            UsuarioRepository usuarioRepository,
            UnidadeRepository unidadeRepository,
            ProdutoRepository produtoRepository,
            EstoqueRepository estoqueRepository,
            ItemPedidoRepository itemPedidoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.unidadeRepository = unidadeRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueRepository = estoqueRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    public Pedido criar(PedidoRequest request) {

        Usuario cliente = usuarioRepository.findById(request.clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Unidade unidade = unidadeRepository.findById(request.unidadeId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setUnidade(unidade);
        pedido.setCanalPedido(request.canalPedido);
        pedido.setStatusPedido(StatusPedido.AGUARDANDO_PAGAMENTO);

        BigDecimal valorTotal = BigDecimal.ZERO;
        List<ItemPedido> itensCriados = new ArrayList<>();

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        for (PedidoRequest.ItemPedidoRequest itemRequest : request.itens) {

            Produto produto = produtoRepository.findById(itemRequest.produtoId)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            Estoque estoque = estoqueRepository.findByProdutoAndUnidade(produto, unidade)
                    .orElseThrow(() -> new RuntimeException("Produto sem estoque nesta unidade"));

            if (estoque.getQuantidade() < itemRequest.quantidade) {
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

            itensCriados.add(item);
            valorTotal = valorTotal.add(subtotal);
        }

        pedidoSalvo.setValorTotal(valorTotal);

        return pedidoRepository.save(pedidoSalvo);
    }

    public Pedido atualizarStatus(Long pedidoId, StatusPedido novoStatus) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        pedido.setStatusPedido(novoStatus);

        return pedidoRepository.save(pedido);
    }
}