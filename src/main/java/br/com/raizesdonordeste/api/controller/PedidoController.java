package br.com.raizesdonordeste.api.controller;

import br.com.raizesdonordeste.api.dto.PedidoRequest;
import br.com.raizesdonordeste.api.dto.PedidoResponse;
import br.com.raizesdonordeste.api.model.Pedido;
import br.com.raizesdonordeste.api.model.enums.StatusPedido;
import br.com.raizesdonordeste.api.repository.PagamentoRepository;
import br.com.raizesdonordeste.api.service.PedidoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PagamentoRepository pagamentoRepository;

    public PedidoController(
            PedidoService pedidoService,
            PagamentoRepository pagamentoRepository) {

        this.pedidoService = pedidoService;
        this.pagamentoRepository = pagamentoRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMINISTRADOR')")
    public PedidoResponse criar(@RequestBody PedidoRequest request) {

        Pedido pedido = pedidoService.criar(request);
        return converterParaResponse(pedido);
    }

    @GetMapping("/unidade/{unidadeId}")
    @PreAuthorize("hasAnyAuthority('FUNCIONARIO', 'GERENTE', 'ADMINISTRADOR')")
    public List<PedidoResponse> listarPorUnidade(@PathVariable Long unidadeId) {

        return pedidoService.listarPorUnidade(unidadeId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('FUNCIONARIO', 'GERENTE', 'ADMINISTRADOR')")
    public PedidoResponse atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusPedido status) {

        Pedido pedido = pedidoService.atualizarStatus(id, status);
        return converterParaResponse(pedido);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMINISTRADOR')")
    public PedidoResponse cancelar(@PathVariable Long id) {

        Pedido pedido = pedidoService.cancelar(id);
        return converterParaResponse(pedido);
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'ADMINISTRADOR')")
    public List<PedidoResponse> listarPorCliente(@PathVariable Long clienteId) {

        return pedidoService.listarPorCliente(clienteId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    private PedidoResponse converterParaResponse(Pedido pedido) {

        return pagamentoRepository.findByPedido(pedido)
                .map(pagamento -> new PedidoResponse(pedido, pagamento.getValor()))
                .orElse(new PedidoResponse(pedido));
    }
}