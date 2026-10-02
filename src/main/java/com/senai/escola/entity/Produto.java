package com.senai.escola.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
@Entity
@Table(name = "produtos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {
    
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false, length = 20)
private String codigo;

@Column(nullable = false, length = 150)
private String descricao;

@Column(length = 14)
private String ncm;

@Column(length = 10)
private String cst;

@Column(length = 10)
private String csosn;

@Column(precision = 10, scale = 2)
private BigDecimal valorUnitario;

@Column(precision = 5, scale = 2)
private BigDecimal aliquotaIcms;

@Column(precision = 5, scale = 2)
private BigDecimal aliquotaIpi;

@Column(length = 10)
private String unidade;

@Column(nullable = false)
private Boolean ativo = true;
}