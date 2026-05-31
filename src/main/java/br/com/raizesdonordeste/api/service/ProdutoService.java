package br.com.raizesdonordeste.api.service;

import br.com.raizesdonordeste.api.dto.ProdutoRequest;
import br.com.raizesdonordeste.api.model.Produto;
import br.com.raizesdonordeste.api.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto cadastrar(ProdutoRequest request) {

        Produto produto = new Produto();

        produto.setNome(request.nome);
        produto.setDescricao(request.descricao);
        produto.setPreco(request.preco);
        produto.setAtivo(true);

        return produtoRepository.save(produto);
    }

    public List<Produto> listar() {
        return produtoRepository.findAll();
    }
}