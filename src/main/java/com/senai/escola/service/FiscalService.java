package com.senai.escola.service;



import com.senai.escola.entity.NotaFiscal;
import com.senai.escola.repository.NotaFiscalRepository;
import com.senai.escola.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FiscalService {
    private final NotaFiscalRepository nfRepo;

    public record Apuracao(
        BigDecimal debitoIcms, BigDecimal creditoIcms, BigDecimal saldoIcms,
        BigDecimal basePisCofins, BigDecimal pis, BigDecimal cofins,
        List<NotaFiscal> notas
    ) {}

    public Apuracao apurar(LocalDate ini, LocalDate fim) {
        Long eid = TenantContext.get();
        var notas = nfRepo.findByPeriodo(eid, ini, fim);

        BigDecimal debIcms = BigDecimal.ZERO, credIcms = BigDecimal.ZERO;
        BigDecimal basePC = BigDecimal.ZERO;

        for (var n : notas) {
            if (n.getTipo() == NotaFiscal.Tipo.SAIDA) {
                debIcms = debIcms.add(n.getValorIcms() != null ? n.getValorIcms() : BigDecimal.ZERO);
                basePC = basePC.add(n.getValorProdutos() != null ? n.getValorProdutos() : BigDecimal.ZERO);
            } else {
                credIcms = credIcms.add(n.getValorIcms() != null ? n.getValorIcms() : BigDecimal.ZERO);
            }
        }
        BigDecimal saldoIcms = debIcms.subtract(credIcms);
        BigDecimal pis = basePC.multiply(new BigDecimal("0.0165")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cofins = basePC.multiply(new BigDecimal("0.076")).setScale(2, RoundingMode.HALF_UP);

        return new Apuracao(debIcms, credIcms, saldoIcms, basePC, pis, cofins, notas);
    }
}