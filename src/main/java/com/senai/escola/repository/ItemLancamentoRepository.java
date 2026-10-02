package com.senai.escola.repository;



package com.erp.fiscal.repository;

import com.erp.fiscal.entity.*;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDate;
import java.util.List;


public interface ItemLancamentoRepository extends JpaRepository<ItemLancamento, Long> {
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN i.tipo='DEBITO' THEN i.valor ELSE 0 END),0)
             - COALESCE(SUM(CASE WHEN i.tipo='CREDITO' THEN i.valor ELSE 0 END),0)
        FROM ItemLancamento i
        WHERE i.conta.id = :contaId
          AND i.lancamento.empresa.id = :eid
          AND i.lancamento.data BETWEEN :i AND :f
    """)
    BigDecimal saldoConta(@Param("contaId") Long contaId,
                          @Param("eid") Long empresaId,
                          @Param("i") LocalDate ini, @Param("f") LocalDate fim);
}
