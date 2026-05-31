package br.com.raizesdonordeste.api.repository;

import br.com.raizesdonordeste.api.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}