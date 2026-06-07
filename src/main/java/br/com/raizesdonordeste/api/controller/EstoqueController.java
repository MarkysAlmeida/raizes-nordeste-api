package br.com.raizesdonordeste.api.controller;

import br.com.raizesdonordeste.api.dto.EstoqueRequest;
import br.com.raizesdonordeste.api.model.Estoque;
import br.com.raizesdonordeste.api.service.EstoqueService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('GERENTE', 'ADMINISTRADOR')")
    public Estoque cadastrar(@RequestBody EstoqueRequest request) {
        return estoqueService.cadastrar(request);
    }

    @GetMapping("/unidade/{unidadeId}")
    public java.util.List<Estoque> listarPorUnidade(@PathVariable Long unidadeId) {
        return estoqueService.listarPorUnidade(unidadeId);
    }


    @DeleteMapping("/produto/{produtoId}/unidade/{unidadeId}")
    @PreAuthorize("hasAnyAuthority('GERENTE', 'ADMINISTRADOR')")
    public void removerProdutoDaUnidade(
            @PathVariable Long produtoId,
            @PathVariable Long unidadeId) {

        estoqueService.removerProdutoDaUnidade(produtoId, unidadeId);
    }
}