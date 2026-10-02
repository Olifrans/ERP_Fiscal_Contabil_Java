package com.senai.escola.entity;



package com.erp.fiscal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime instante;
    private String usuario;
    private Long empresaId;
    private String acao;         // CREATE, UPDATE, DELETE
    private String entidade;
    private Long entityId;
    @Column(columnDefinition = "TEXT")
    private String payload;
}