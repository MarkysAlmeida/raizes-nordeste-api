package br.com.raizesdonordeste.api.controller;

import br.com.raizesdonordeste.api.dto.UnidadeRequest;
import br.com.raizesdonordeste.api.dto.UnidadeUpdateRequest;
import br.com.raizesdonordeste.api.model.Unidade;
import br.com.raizesdonordeste.api.service.UnidadeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unidades")
public class UnidadeController {

    private final UnidadeService unidadeService;

    public UnidadeController(UnidadeService unidadeService) {
        this.unidadeService = unidadeService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public Unidade cadastrar(@RequestBody UnidadeRequest request) {
        return unidadeService.cadastrar(request);
    }

    @GetMapping
    public List<Unidade> listar() {
        return unidadeService.listar();
    }

    @PatchMapping("/{unidadeId}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public Unidade editar(
            @PathVariable Long unidadeId,
            @RequestBody UnidadeUpdateRequest request) {

        return unidadeService.editar(unidadeId, request);
    }

    @PatchMapping("/{unidadeId}/desativar")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public Unidade desativar(@PathVariable Long unidadeId) {

        return unidadeService.desativar(unidadeId);
    }


    @DeleteMapping("/{unidadeId}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public void excluir(@PathVariable Long unidadeId) {
        unidadeService.excluir(unidadeId);
    }
}