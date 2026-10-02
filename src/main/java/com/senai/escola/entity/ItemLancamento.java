package com.senai.escola.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "itens_lancamento")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemLancamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lancamento_id", nullable = false)
    private Lancamento lancamento;

    @ManyToOne
    @JoinColumn(name = "conta_id", nullable = false)
    private ContaContabil conta;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;  // DEBITO ou CREDITO

    private BigDecimal valor;

    public enum Tipo { DEBITO, CREDITO }
}