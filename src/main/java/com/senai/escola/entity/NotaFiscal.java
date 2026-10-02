package com.senai.escola.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "notas_fiscais")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotaFiscal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numero;
    private LocalDate dataEmissao;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;  // ENTRADA ou SAIDA

    private String chaveAcesso;
    private String cfop;
    private BigDecimal valorProdutos;
    private BigDecimal valorServicos;
    private BigDecimal baseIcms;
    private BigDecimal valorIcms;
    private BigDecimal valorPis;
    private BigDecimal valorCofins;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    public enum Tipo { ENTRADA, SAIDA }
}