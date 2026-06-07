package br.com.raizesdonordeste.api.service;

import br.com.raizesdonordeste.api.dto.UsuarioRequest;
import br.com.raizesdonordeste.api.dto.UsuarioUpdateRequest;
import br.com.raizesdonordeste.api.model.Unidade;
import br.com.raizesdonordeste.api.model.Usuario;
import br.com.raizesdonordeste.api.model.enums.Role;
import br.com.raizesdonordeste.api.repository.UnidadeRepository;
import br.com.raizesdonordeste.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private static final Logger logger =
            LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            UnidadeRepository unidadeRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.unidadeRepository = unidadeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrarClientePublico(UsuarioRequest request) {
        request.role = Role.CLIENTE;
        request.unidadeId = null;
        return cadastrar(request);
    }

    public Usuario cadastrarClientePorFuncionario(UsuarioRequest request) {
        request.role = Role.CLIENTE;
        request.unidadeId = null;
        return cadastrar(request);
    }

    public Usuario cadastrarPorGerente(UsuarioRequest request) {

        if (request.role == Role.ADMINISTRADOR || request.role == Role.GERENTE) {
            throw new RuntimeException("Gerente só pode cadastrar funcionário ou cliente");
        }

        return cadastrar(request);
    }

    public Usuario cadastrarPorAdmin(UsuarioRequest request) {

        if (request.role == Role.ADMINISTRADOR) {
            throw new RuntimeException("Não é permitido cadastrar outro administrador por esta rota");
        }

        return cadastrar(request);
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
        usuario.setAtivo(true);

        if (request.role == Role.FUNCIONARIO || request.role == Role.GERENTE) {

            if (request.unidadeId == null) {
                throw new RuntimeException("Funcionário ou gerente precisa estar vinculado a uma unidade");
            }

            Unidade unidade = unidadeRepository.findById(request.unidadeId)
                    .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

            if (request.role == Role.GERENTE) {

                boolean gerenteAtivoExiste = usuarioRepository
                        .existsByRoleAndUnidadeIdAndAtivoTrue(Role.GERENTE, unidade.getId());

                if (gerenteAtivoExiste) {
                    throw new RuntimeException("Esta unidade já possui um gerente ativo");
                }
            }

            usuario.setUnidade(unidade);
        }

        logger.info("Usuário cadastrado: {} Perfil: {}", usuario.getEmail(), usuario.getRole());

        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String senha) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (Boolean.FALSE.equals(usuario.getAtivo())) {
            throw new RuntimeException("Usuário inativo. Entre em contato com o administrador");
        }

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            logger.warn("Tentativa de login com senha inválida: {}", email);
            throw new RuntimeException("Senha inválida");
        }

        logger.info("Login realizado: {}", usuario.getEmail());

        return usuario;
    }

    public Usuario editarUsuario(Long usuarioId, UsuarioUpdateRequest request) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (request.nome != null) {
            usuario.setNome(request.nome);
        }

        if (request.email != null) {
            usuario.setEmail(request.email);
        }

        if (request.senha != null && !request.senha.isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.senha));
        }

        if (request.role != null) {

            if (usuario.getRole() == Role.ADMINISTRADOR && request.role != Role.ADMINISTRADOR) {
                throw new RuntimeException("Não é permitido alterar o perfil do administrador");
            }

            usuario.setRole(request.role);
        }

        if (request.unidadeId != null) {
            Unidade unidade = unidadeRepository.findById(request.unidadeId)
                    .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

            if (usuario.getRole() == Role.GERENTE) {

                boolean gerenteAtivoExiste = usuarioRepository
                        .existsByRoleAndUnidadeIdAndAtivoTrue(Role.GERENTE, unidade.getId());

                boolean mesmoUsuario = usuario.getUnidade() != null
                        && usuario.getUnidade().getId().equals(unidade.getId())
                        && Boolean.TRUE.equals(usuario.getAtivo());

                if (gerenteAtivoExiste && !mesmoUsuario) {
                    throw new RuntimeException("Esta unidade já possui um gerente ativo");
                }
            }

            usuario.setUnidade(unidade);
        }

        if (request.ativo != null) {

            if (usuario.getRole() == Role.ADMINISTRADOR && Boolean.FALSE.equals(request.ativo)) {
                throw new RuntimeException("Administrador não pode ser desativado");
            }

            usuario.setAtivo(request.ativo);
        }

        logger.info("Usuário editado: {}", usuario.getEmail());

        return usuarioRepository.save(usuario);
    }

    public void excluirUsuario(Long usuarioId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getRole() == Role.ADMINISTRADOR) {
            throw new RuntimeException("Administrador não pode ser excluído");
        }

        if (Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new RuntimeException("O usuário precisa estar desativado para ser excluído");
        }

        try {
            usuarioRepository.delete(usuario);
        } catch (Exception e) {
            throw new RuntimeException("Não é possível excluir este usuário porque ele possui pedidos vinculados");
        }

        logger.info("Usuário excluído: {}", usuario.getEmail());
    }

    public java.util.List<Usuario> listarTodos() {
        logger.info("Listagem de usuários solicitada");
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}