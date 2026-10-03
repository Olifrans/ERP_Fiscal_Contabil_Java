package com.senai.escola.audit;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data               // Gera getters, setters, toString, equals e hashCode
@NoArgsConstructor  // Gera o construtor vazio público (exigido pelo JPA/Hibernate)
@AllArgsConstructor // Gera o construtor com todos os argumentos (útil para o Builder/Logs)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime instante;
    
    private String usuario;
    
    private Long empresaId;
    
    private String acao; // Ex: CREATE, UPDATE, DELETE
    
    private String entidade; // Ex: "Lancamento", "NotaFiscal"
    
    private Long entityId;
    
    @Column(columnDefinition = "TEXT")
    private String payload;
}