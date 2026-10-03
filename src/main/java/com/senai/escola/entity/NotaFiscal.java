package com.senai.escola.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "notas_fiscais")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EntityListeners(com.senai.escola.audit.AuditListener.class)
public class NotaFiscal extends TenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer numero;
    
    private LocalDate dataEmissao;
    
    @Enumerated(EnumType.STRING)
    private Tipo tipo;
    
    private String chaveAcesso;
    
    private String cfop;
    
    private String destinatario;
    
    private String cnpjDestinatario;
    
    private BigDecimal valorProdutos;
    
    private BigDecimal valorServicos;
    
    private BigDecimal baseIcms;
    
    private BigDecimal valorIcms;
    
    private BigDecimal valorPis;
    
    private BigDecimal valorCofins;
    
    private String status;
    
    private String protocolo;
    
    @Column(columnDefinition = "TEXT")
    private String xml;

    public enum Tipo {
        ENTRADA, SAIDA
    }
}