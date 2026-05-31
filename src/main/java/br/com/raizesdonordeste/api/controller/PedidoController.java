package br.com.raizesdonordeste.api.controller;

import br.com.raizesdonordeste.api.dto.PedidoRequest;
import br.com.raizesdonordeste.api.model.Pedido;
import br.com.raizesdonordeste.api.model.enums.StatusPedido;
import br.com.raizesdonordeste.api.service.PedidoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMINISTRADOR')")
    public Pedido criar(@RequestBody PedidoRequest request) {
        return pedidoService.criar(request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('FUNCIONARIO', 'GERENTE', 'ADMINISTRADOR')")
    public Pedido atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusPedido status) {

        return pedidoService.atualizarStatus(id, status);
    }
}