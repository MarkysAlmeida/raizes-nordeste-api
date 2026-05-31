package br.com.raizesdonordeste.api.model;

import br.com.raizesdonordeste.api.model.enums.CanalPedido;
import br.com.raizesdonordeste.api.model.enums.StatusPedido;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Usuario cliente;

    @ManyToOne
    private Unidade unidade;

    @Enumerated(EnumType.STRING)
    private CanalPedido canalPedido;

    @Enumerated(EnumType.STRING)
    private StatusPedido statusPedido;

    private BigDecimal valorTotal;
}