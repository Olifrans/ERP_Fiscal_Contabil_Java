package com.senai.escola.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.senai.escola.entity.NotaFiscal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;


public interface NotaFiscalRepository extends JpaRepository<NotaFiscal, Long> {
    @Query("SELECT n FROM NotaFiscal n WHERE n.empresaId = :eid AND n.dataEmissao BETWEEN :i AND :f ORDER BY n.dataEmissao")
    List<NotaFiscal> findByPeriodo(@Param("eid") Long empresaId,
                                   @Param("i") LocalDate ini, @Param("f") LocalDate fim);
}