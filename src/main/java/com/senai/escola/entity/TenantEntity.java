package com.senai.escola.entity;


import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EntityListeners(com.senai.escola.audit.AuditListener.class)
public abstract class TenantEntity {
    
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;
}