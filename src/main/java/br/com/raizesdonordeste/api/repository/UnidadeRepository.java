package br.com.raizesdonordeste.api.repository;

import br.com.raizesdonordeste.api.model.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
}