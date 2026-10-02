package com.senai.escola.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lancamentos")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Lancamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate data;
    private String historico;
    private String documento;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @OneToMany(mappedBy = "lancamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemLancamento> itens = new ArrayList<>();

    public BigDecimal totalDebito() {
        return itens.stream()
                .map(ItemLancamento::getValor)
                .filter(v -> itens.get(itens.indexOf(null)) != null) // placeholder
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}