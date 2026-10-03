package com.senai.escola.service;


import com.senai.escola.entity.ContaContabil;
import com.senai.escola.entity.ItemLancamento;
import com.senai.escola.repository.*;
import com.senai.escola.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RazaoBalanceteService {
    private final ItemLancamentoRepository itemRepo;
    private final ContaContabilRepository contaRepo;

    public record ItemRazao(LocalDate data, String historico, String documento,
                            BigDecimal debito, BigDecimal credito, BigDecimal saldo) {}

    public record ItemBalancete(Long contaId, String codigo, String descricao,
                                BigDecimal totalDeb, BigDecimal totalCred, BigDecimal saldo) {}

    public List<ItemRazao> razao(Long contaId, LocalDate ini, LocalDate fim) {
        Long eid = TenantContext.get();
        var itens = itemRepo.razaoConta(contaId, eid, ini, fim);
        BigDecimal saldo = BigDecimal.ZERO;
        List<ItemRazao> out = new ArrayList<>();
        for (var i : itens) {
            BigDecimal d = i.getTipo() == ItemLancamento.Tipo.DEBITO ? i.getValor() : BigDecimal.ZERO;
            BigDecimal c = i.getTipo() == ItemLancamento.Tipo.CREDITO ? i.getValor() : BigDecimal.ZERO;
            saldo = saldo.add(d).subtract(c);
            out.add(new ItemRazao(i.getLancamento().getData(), i.getLancamento().getHistorico(),
                    i.getLancamento().getDocumento(), d, c, saldo));
        }
        return out;
    }

    public List<ItemBalancete> balancete(LocalDate ini, LocalDate fim) {
        Long eid = TenantContext.get();
        var linhas = itemRepo.balancete(eid, ini, fim);
        List<ItemBalancete> out = new ArrayList<>();
        for (Object[] r : linhas) {
            Long contaId = ((Number) r[0]).longValue();
            BigDecimal tDeb = (BigDecimal) r[1];
            BigDecimal tCred = (BigDecimal) r[2];
            ContaContabil c = contaRepo.findById(contaId).orElseThrow();
            BigDecimal saldo = c.getNatureza() == ContaContabil.Natureza.DEVEDORA
                    ? tDeb.subtract(tCred) : tCred.subtract(tDeb);
            out.add(new ItemBalancete(contaId, c.getCodigo(), c.getDescricao(), tDeb, tCred, saldo));
        }
        return out;
    }
}