package br.com.raizesdonordeste.api.repository;

import br.com.raizesdonordeste.api.model.Estoque;
import br.com.raizesdonordeste.api.model.Produto;
import br.com.raizesdonordeste.api.model.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByProdutoAndUnidade(Produto produto, Unidade unidade);
}