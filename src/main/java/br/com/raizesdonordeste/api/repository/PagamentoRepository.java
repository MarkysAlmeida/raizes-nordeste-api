package br.com.raizesdonordeste.api.repository;

import br.com.raizesdonordeste.api.model.Pagamento;
import br.com.raizesdonordeste.api.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByPedido(Pedido pedido);
}