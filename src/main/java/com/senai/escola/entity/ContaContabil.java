package com.senai.escola.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contas_contabeis")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ContaContabil {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;       // ex: 1.1.01.001

    @Column(nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    private Natureza natureza;   // DEVEDORA ou CREDORA

    @Enumerated(EnumType.STRING)
    private Grupo grupo;         // ATIVO, PASSIVO, RECEITA, DESPESA, RESULTADO

    private Long contaPaiId;     // hierarquia

    public enum Natureza { DEVEDORA, CREDORA }
    public enum Grupo { ATIVO, PASSIVO, RECEITA, DESPESA, RESULTADO }
}