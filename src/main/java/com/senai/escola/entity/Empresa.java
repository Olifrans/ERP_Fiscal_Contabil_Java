package com.senai.escola.entity;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(com.senai.escola.audit.AuditListener.class)
public class Empresa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String razaoSocial;
    
    @Column(nullable = false, unique = true)
    private String cnpj;
    
    private String inscricaoEstadual;
    
    private String endereco;
    
    private String municipio;
    
    private String uf;
    
    private String regimeTributario;
    
    private String telefone;
    
    private String email;
}