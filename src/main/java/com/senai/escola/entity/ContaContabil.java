package com.senai.escola.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "contas_contabeis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EntityListeners(com.senai.escola.audit.AuditListener.class)
public class ContaContabil extends TenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 20)
    private String codigo;
    
    @Column(nullable = false)
    private String descricao;
    
    @Enumerated(EnumType.STRING)
    private Natureza natureza;
    
    @Enumerated(EnumType.STRING)
    private Grupo grupo;
    
    private Long contaPaiId;
    
    private boolean analitica = true;

    public enum Natureza {
        DEVEDORA, CREDORA
    }

    public enum Grupo {
        ATIVO, PASSIVO, RECEITA, DESPESA, RESULTADO
    }
}