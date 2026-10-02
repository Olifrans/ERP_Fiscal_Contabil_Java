package com.senai.escola.repository;



package com.erp.fiscal.repository;

import com.erp.fiscal.entity.*;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDate;
import java.util.List;


public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    @Query("SELECT l FROM Lancamento l WHERE l.empresa.id = :eid AND l.data BETWEEN :i AND :f ORDER BY l.data")
    List<Lancamento> findByPeriodo(@Param("eid") Long empresaId,
                                   @Param("i") LocalDate ini, @Param("f") LocalDate fim);
}
