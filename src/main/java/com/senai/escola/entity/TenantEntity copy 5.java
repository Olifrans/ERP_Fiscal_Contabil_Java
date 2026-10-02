package com.senai.escola.entity;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass
@Data
public abstract class TenantEntity {
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;
}