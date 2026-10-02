package com.senai.escola.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false) private String login;
    @Column(nullable = false) private String senhaHash;
    private String nome;
    private String perfil;       // ADMIN, CONTADOR, FINANCEIRO
    @Column(name = "empresa_id") private Long empresaId;
    private boolean ativo = true;
}