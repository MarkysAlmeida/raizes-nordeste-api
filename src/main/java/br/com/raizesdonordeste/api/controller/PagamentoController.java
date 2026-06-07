package br.com.raizesdonordeste.api.controller;

import br.com.raizesdonordeste.api.dto.PagamentoRequest;
import br.com.raizesdonordeste.api.dto.PagamentoResponse;
import br.com.raizesdonordeste.api.model.Pagamento;
import br.com.raizesdonordeste.api.service.PagamentoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("/{pedidoId}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMINISTRADOR')")
    public PagamentoResponse processar(
            @PathVariable Long pedidoId,
            @RequestBody PagamentoRequest request) {

        Pagamento pagamento = pagamentoService.processar(pedidoId, request);
        return new PagamentoResponse(pagamento);
    }
}