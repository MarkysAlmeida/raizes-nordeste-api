package br.com.raizesdonordeste.api.repository;

import br.com.raizesdonordeste.api.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
}