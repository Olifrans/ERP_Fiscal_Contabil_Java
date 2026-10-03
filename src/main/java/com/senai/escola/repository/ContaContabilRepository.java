package com.senai.escola.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.senai.escola.entity.ContaContabil;
import java.util.Optional;
import java.util.List;


public interface ContaContabilRepository extends JpaRepository<ContaContabil, Long> {
    List<ContaContabil> findByEmpresaIdAndGrupo(Long empresaId, ContaContabil.Grupo grupo);
    List<ContaContabil> findByEmpresaIdAndGrupoIn(Long empresaId, List<ContaContabil.Grupo> grupos);
    List<ContaContabil> findByEmpresaIdOrderByCodigo(Long empresaId);
    Optional<ContaContabil> findByEmpresaIdAndCodigo(Long empresaId, String codigo);
}