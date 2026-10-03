package com.senai.escola.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.senai.escola.entity.FechamentoMensal;


public interface FechamentoMensalRepository extends JpaRepository<FechamentoMensal, Long> {
    boolean existsByEmpresaIdAndAnoAndMes(Long empresaId, Integer ano, Integer mes);
    Optional<FechamentoMensal> findByEmpresaIdAndAnoAndMes(Long empresaId, Integer ano, Integer mes);
}