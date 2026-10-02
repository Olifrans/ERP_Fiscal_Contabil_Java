package com.senai.escola.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Entity
@Table(name = "pessoas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pessoa  {    
   
   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 18)
    private String cpfCnpj;

    @Column(length = 16)
    private String inscricaoEstadual;

    @Column(length = 200)
    private String endereco;

    @Column(length = 100)
    private String bairro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(length = 10)
    private String cep;

    @Column(length = 15)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String tipo; // FISICA, JURIDICA

    @Column(nullable = false, length = 20)
    private String classificacao; // CLIENTE, FORNECEDOR, AMBOS

    @Column(nullable = false)
    private Boolean ativo = true;
}