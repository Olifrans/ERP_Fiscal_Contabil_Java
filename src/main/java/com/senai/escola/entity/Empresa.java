package com.senai.escola.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresas")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Empresa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String razaoSocial;
    @Column(nullable = false, unique = true) private String cnpj;
    private String inscricaoEstadual;
    private String endereco;
    private String regimeTributario; // SIMPLES, LUCRO_PRESUMIDO, LUCRO_REAL
}