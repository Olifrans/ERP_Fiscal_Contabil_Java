package com.senai.escola.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.senai.escola.entity.ItemLancamento;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ItemLancamentoRepository extends JpaRepository<ItemLancamento, Long> {

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN i.tipo='DEBITO' THEN i.valor ELSE 0 END),0)
             - COALESCE(SUM(CASE WHEN i.tipo='CREDITO' THEN i.valor ELSE 0 END),0)
        FROM ItemLancamento i
        WHERE i.conta.id = :contaId
          AND i.lancamento.empresaId = :eid
          AND i.lancamento.data BETWEEN :i AND :f
    """)
    BigDecimal saldoConta(@Param("contaId") Long contaId, @Param("eid") Long eid,
                          @Param("i") LocalDate ini, @Param("f") LocalDate fim);

    @Query("""
        SELECT i FROM ItemLancamento i
        WHERE i.conta.id = :contaId AND i.lancamento.empresaId = :eid
          AND i.lancamento.data BETWEEN :i AND :f
        ORDER BY i.lancamento.data, i.lancamento.id
    """)
    List<ItemLancamento> razaoConta(@Param("contaId") Long contaId, @Param("eid") Long eid,
                                    @Param("i") LocalDate ini, @Param("f") LocalDate fim);

    @Query("""
        SELECT i.conta.id,
               COALESCE(SUM(CASE WHEN i.tipo='DEBITO'  THEN i.valor ELSE 0 END),0),
               COALESCE(SUM(CASE WHEN i.tipo='CREDITO' THEN i.valor ELSE 0 END),0)
        FROM ItemLancamento i
        WHERE i.lancamento.empresaId = :eid
          AND i.lancamento.data BETWEEN :i AND :f
        GROUP BY i.conta.id
    """)
    List<Object[]> balancete(@Param("eid") Long eid,
                             @Param("i") LocalDate ini, @Param("f") LocalDate fim);
}