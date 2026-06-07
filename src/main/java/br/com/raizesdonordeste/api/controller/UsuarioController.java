package br.com.raizesdonordeste.api.controller;

import br.com.raizesdonordeste.api.dto.LoginRequest;
import br.com.raizesdonordeste.api.dto.LoginResponse;
import br.com.raizesdonordeste.api.dto.UsuarioRequest;
import br.com.raizesdonordeste.api.dto.UsuarioResponse;
import br.com.raizesdonordeste.api.dto.UsuarioUpdateRequest;
import br.com.raizesdonordeste.api.model.Usuario;
import br.com.raizesdonordeste.api.security.JwtService;
import br.com.raizesdonordeste.api.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public UsuarioController(
            UsuarioService usuarioService,
            JwtService jwtService) {

        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    // Cadastro público: somente CLIENTE
    @PostMapping("/cliente")
    public UsuarioResponse cadastrarCliente(@RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.cadastrarClientePublico(request);
        return converterParaResponse(usuario);
    }

    // Cadastro feito por funcionário: somente CLIENTE
    @PostMapping("/funcionario/cadastrar-cliente")
    @PreAuthorize("hasAnyAuthority('FUNCIONARIO', 'GERENTE', 'ADMINISTRADOR')")
    public UsuarioResponse funcionarioCadastrarCliente(@RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.cadastrarClientePorFuncionario(request);
        return converterParaResponse(usuario);
    }

    // Cadastro feito por gerente: FUNCIONARIO ou CLIENTE
    @PostMapping("/gerente/cadastrar")
    @PreAuthorize("hasAnyAuthority('GERENTE', 'ADMINISTRADOR')")
    public UsuarioResponse gerenteCadastrar(@RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.cadastrarPorGerente(request);
        return converterParaResponse(usuario);
    }

    // Cadastro feito por admin: GERENTE, FUNCIONARIO ou CLIENTE
    @PostMapping("/admin/cadastrar")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public UsuarioResponse adminCadastrar(@RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.cadastrarPorAdmin(request);
        return converterParaResponse(usuario);
    }

    // Login
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.login(request.email, request.senha);

        String token = jwtService.gerarToken(usuario);

        return new LoginResponse(token);
    }

    // Edita usuário
    @PatchMapping("/admin/{usuarioId}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public UsuarioResponse editarUsuario(
            @PathVariable Long usuarioId,
            @RequestBody UsuarioUpdateRequest request) {

        Usuario usuario = usuarioService.editarUsuario(usuarioId, request);
        return converterParaResponse(usuario);
    }

    // Exclui usuário
    @DeleteMapping("/admin/{usuarioId}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public void excluirUsuario(@PathVariable Long usuarioId) {
        usuarioService.excluirUsuario(usuarioId);
    }

    // Lista todos usuários
    @GetMapping
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public java.util.List<UsuarioResponse> listarTodos() {

        return usuarioService.listarTodos()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // Busca usuário por ID
    @GetMapping("/{usuarioId}")
    @PreAuthorize("hasAnyAuthority('CLIENTE', 'FUNCIONARIO', 'GERENTE', 'ADMINISTRADOR')")
    public UsuarioResponse buscarPorId(@PathVariable Long usuarioId) {

        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        return converterParaResponse(usuario);
    }

    private UsuarioResponse converterParaResponse(Usuario usuario) {

        String nomeUnidade = usuario.getUnidade() != null
                ? usuario.getUnidade().getNome()
                : null;

        Integer pontos = usuario.getRole() == br.com.raizesdonordeste.api.model.enums.Role.CLIENTE
                ? usuario.getPontosFidelidade()
                : null;

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                nomeUnidade,
                pontos,
                usuario.getAtivo()
        );
    }
}