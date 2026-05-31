package br.com.raizesdonordeste.api.service;

import br.com.raizesdonordeste.api.dto.UsuarioRequest;
import br.com.raizesdonordeste.api.model.Usuario;
import br.com.raizesdonordeste.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrar(UsuarioRequest request) {

        if (usuarioRepository.existsByEmail(request.email)) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        Usuario usuario = new Usuario();

        usuario.setNome(request.nome);
        usuario.setEmail(request.email);
        usuario.setSenha(passwordEncoder.encode(request.senha));
        usuario.setRole(request.role);

        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String senha) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new RuntimeException("Senha inválida");
        }

        return usuario;
    }
}