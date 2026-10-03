package com.senai.escola.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "fechamentos_mensais",
       uniqueConstraints = @UniqueConstraint(columnNames = {"empresa_id", "ano", "mes"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(com.senai.escola.audit.AuditListener.class)
public class FechamentoMensal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "empresa_id")
    private Long empresaId;
    
    private Integer ano;
    
    private Integer mes;
    
    private LocalDate dataFechamento;
    
    private String usuario;
    
    private boolean estornado;
    
    private Long lancamentoId;
}