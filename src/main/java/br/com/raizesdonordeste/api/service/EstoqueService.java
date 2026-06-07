package br.com.raizesdonordeste.api.service;

import br.com.raizesdonordeste.api.dto.EstoqueRequest;
import br.com.raizesdonordeste.api.model.Estoque;
import br.com.raizesdonordeste.api.model.Produto;
import br.com.raizesdonordeste.api.model.Unidade;
import br.com.raizesdonordeste.api.repository.EstoqueRepository;
import br.com.raizesdonordeste.api.repository.ProdutoRepository;
import br.com.raizesdonordeste.api.repository.UnidadeRepository;
import org.springframework.stereotype.Service;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;
    private final UnidadeRepository unidadeRepository;

    public EstoqueService(
            EstoqueRepository estoqueRepository,
            ProdutoRepository produtoRepository,
            UnidadeRepository unidadeRepository) {

        this.estoqueRepository = estoqueRepository;
        this.produtoRepository = produtoRepository;
        this.unidadeRepository = unidadeRepository;
    }

    public Estoque cadastrar(EstoqueRequest request) {

        Produto produto = produtoRepository.findById(request.produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Unidade unidade = unidadeRepository.findById(request.unidadeId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        if (estoqueRepository.existsByProdutoAndUnidade(produto, unidade)) {
            throw new RuntimeException("Este produto já está cadastrado nesta unidade");
        }

        Estoque estoque = new Estoque();

        estoque.setProduto(produto);
        estoque.setUnidade(unidade);
        estoque.setQuantidade(request.quantidade);

        return estoqueRepository.save(estoque);
    }


    public java.util.List<Estoque> listarPorUnidade(Long unidadeId) {

        Unidade unidade = unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        return estoqueRepository.findAll()
                .stream()
                .filter(estoque -> estoque.getUnidade().getId().equals(unidade.getId()))
                .toList();
    }


    public void removerProdutoDaUnidade(Long produtoId, Long unidadeId) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Unidade unidade = unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        Estoque estoque = estoqueRepository.findByProdutoAndUnidade(produto, unidade)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado nesta unidade"));

        if (Boolean.TRUE.equals(produto.getAtivo())) {
            throw new RuntimeException("O produto precisa estar desativado para ser excluído");
        }

        estoqueRepository.delete(estoque);
    }
}