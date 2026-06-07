package br.com.raizesdonordeste.api.controller;

import br.com.raizesdonordeste.api.dto.ProdutoRequest;
import br.com.raizesdonordeste.api.dto.ProdutoUpdateRequest;
import br.com.raizesdonordeste.api.model.Produto;
import br.com.raizesdonordeste.api.service.ProdutoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }
    //Cadastrar Produto
    @PostMapping
    @PreAuthorize("hasAnyAuthority('GERENTE', 'ADMINISTRADOR')")
    public Produto cadastrar(@RequestBody ProdutoRequest request) {
        return produtoService.cadastrar(request);
    }

    @GetMapping
    public List<Produto> listar() {
        return produtoService.listar();
    }
    //Editar Produto da loja
    @PatchMapping("/{produtoId}/usuario/{usuarioId}")
    @PreAuthorize("hasAnyAuthority('GERENTE', 'ADMINISTRADOR')")
    public Produto editarProdutoDaLoja(
            @PathVariable Long produtoId,
            @PathVariable Long usuarioId,
            @RequestBody ProdutoUpdateRequest request) {

        return produtoService.editarProdutoDaLoja(
                produtoId,
                usuarioId,
                request
        );
    }
    //Desativar produto da loja
    @PatchMapping("/{produtoId}/usuario/{usuarioId}/desativar")
    @PreAuthorize("hasAnyAuthority('GERENTE', 'ADMINISTRADOR')")
    public Produto desativarProdutoDaLoja(
            @PathVariable Long produtoId,
            @PathVariable Long usuarioId) {

        return produtoService.desativarProdutoDaLoja(produtoId, usuarioId);
    }

}