package br.com.raizesdonordeste.api.controller;

import br.com.raizesdonordeste.api.dto.LoginRequest;
import br.com.raizesdonordeste.api.dto.LoginResponse;
import br.com.raizesdonordeste.api.dto.UsuarioRequest;
import br.com.raizesdonordeste.api.dto.UsuarioResponse;
import br.com.raizesdonordeste.api.model.Usuario;
import br.com.raizesdonordeste.api.security.JwtService;
import br.com.raizesdonordeste.api.service.UsuarioService;
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

    @PostMapping
    public UsuarioResponse cadastrar(@RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.cadastrar(request);

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getPontosFidelidade()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.login(request.email, request.senha);

        String token = jwtService.gerarToken(usuario);

        return new LoginResponse(token);
    }
}