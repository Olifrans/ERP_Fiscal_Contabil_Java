package com.senai.escola.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.escola.entity.Lancamento;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    @Query("SELECT l FROM Lancamento l WHERE l.empresaId = :eid AND l.data BETWEEN :i AND :f ORDER BY l.data, l.id")
    List<Lancamento> findByPeriodo(@Param("eid") Long empresaId,
                                   @Param("i") LocalDate ini, @Param("f") LocalDate fim);
    @Modifying
    @Query("DELETE FROM Lancamento l WHERE l.empresaId = :eid AND l.documento = :doc")
    void deleteByEmpresaIdAndDocumento(@Param("eid") Long empresaId, @Param("doc") String doc);
}