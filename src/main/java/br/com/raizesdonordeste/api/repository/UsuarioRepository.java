package br.com.raizesdonordeste.api.repository;

import br.com.raizesdonordeste.api.model.Usuario;
import br.com.raizesdonordeste.api.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRoleAndUnidadeId(Role role, Long unidadeId);

    boolean existsByRoleAndUnidadeIdAndAtivoTrue(Role role, Long unidadeId);
}