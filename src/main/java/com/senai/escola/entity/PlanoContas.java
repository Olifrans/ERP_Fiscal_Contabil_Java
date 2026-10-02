package com.senai.escola.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
@Entity
@Table(name = "plano_contas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanoContas {
  @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false, length = 20)
private String codigo;

@Column(nullable = false, length = 150)
private String descricao;

@Column(nullable = false, length = 20)
private String tipo; // ANALITICA, SINTETICA

@Column(nullable = false, length = 20)
private String natureza; // ATIVO, PASSIVO, RECEITA, DESPESA, PATRIMONIO_LIQUIDO

@ManyToOne
@JoinColumn(name = "conta_pai_id")
private PlanoContas contaPai;

@Column(precision = 14, scale = 2)
private BigDecimal saldo = BigDecimal.ZERO;

@Column(nullable = false)
private Boolean ativo = true;
   
}