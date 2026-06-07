package br.com.raizesdonordeste.api.repository;

import br.com.raizesdonordeste.api.model.Pedido;
import br.com.raizesdonordeste.api.model.Unidade;
import br.com.raizesdonordeste.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByCliente(Usuario cliente);

    List<Pedido> findByUnidade(Unidade unidade);
}