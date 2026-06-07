package br.com.raizesdonordeste.api.service;

import br.com.raizesdonordeste.api.dto.ProdutoRequest;
import br.com.raizesdonordeste.api.dto.ProdutoUpdateRequest;
import br.com.raizesdonordeste.api.model.Estoque;
import br.com.raizesdonordeste.api.model.Produto;
import br.com.raizesdonordeste.api.model.Usuario;
import br.com.raizesdonordeste.api.model.enums.Role;
import br.com.raizesdonordeste.api.repository.EstoqueRepository;
import br.com.raizesdonordeste.api.repository.ProdutoRepository;
import br.com.raizesdonordeste.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final UsuarioRepository usuarioRepository;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            EstoqueRepository estoqueRepository,
            UsuarioRepository usuarioRepository) {

        this.produtoRepository = produtoRepository;
        this.estoqueRepository = estoqueRepository;
        this.usuarioRepository = usuarioRepository;
    }
    //Cadastrar Produto, somente gerente e ADM
    public Produto cadastrar(ProdutoRequest request) {

        Produto produto = new Produto();

        produto.setNome(request.nome);
        produto.setDescricao(request.descricao);
        produto.setPreco(request.preco);
        produto.setAtivo(true);

        return produtoRepository.save(produto);
    }
    //Listar Produtos
    public List<Produto> listar() {
        return produtoRepository.findAll();
    }

    //Editar Produto Por loja
    public Produto editarProdutoDaLoja(
            Long produtoId,
            Long usuarioId,
            ProdutoUpdateRequest request) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (usuario.getRole() == Role.GERENTE) {

            if (usuario.getUnidade() == null) {
                throw new RuntimeException("Gerente não está vinculado a nenhuma unidade");
            }

            Estoque estoque = estoqueRepository
                    .findByProdutoAndUnidade(produto, usuario.getUnidade())
                    .orElseThrow(() -> new RuntimeException("Este produto não pertence à loja do gerente"));

            if (request.quantidade != null) {
                estoque.setQuantidade(request.quantidade);
                estoqueRepository.save(estoque);
            }
        }

        if (request.nome != null) {
            produto.setNome(request.nome);
        }

        if (request.descricao != null) {
            produto.setDescricao(request.descricao);
        }

        if (request.preco != null) {
            produto.setPreco(request.preco);
        }

        if (request.ativo != null) {
            produto.setAtivo(request.ativo);
        }

        return produtoRepository.save(produto);
    }

    //Desativar produto
    public Produto desativarProdutoDaLoja(Long produtoId, Long usuarioId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (usuario.getRole() == Role.GERENTE) {

            if (usuario.getUnidade() == null) {
                throw new RuntimeException("Gerente não está vinculado a nenhuma unidade");
            }

            estoqueRepository
                    .findByProdutoAndUnidade(produto, usuario.getUnidade())
                    .orElseThrow(() -> new RuntimeException("Este produto não pertence à loja do gerente"));
        }

        produto.setAtivo(false);

        return produtoRepository.save(produto);
    }

}