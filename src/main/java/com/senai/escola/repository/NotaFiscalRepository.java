package com.senai.escola.repository;



package com.erp.fiscal.repository;

import com.erp.fiscal.entity.*;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDate;
import java.util.List;

public interface NotaFiscalRepository extends JpaRepository<NotaFiscal, Long> {
    @Query("SELECT n FROM NotaFiscal n WHERE n.empresa.id = :eid AND n.dataEmissao BETWEEN :i AND :f")
    List<NotaFiscal> findByPeriodo(@Param("eid") Long empresaId,
                                   @Param("i") LocalDate ini, @Param("f") LocalDate fim);
}