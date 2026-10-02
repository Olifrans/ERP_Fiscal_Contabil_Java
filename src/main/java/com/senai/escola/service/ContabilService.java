package com.senai.escola.service;



import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;




import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service @RequiredArgsConstructor
public class ContabilService {

    private final LancamentoRepository lancRepo;
    private final ItemLancamentoRepository itemRepo;
    private final ContaContabilRepository contaRepo;

    @Transactional
    public Lancamento lancar(LancamentoRequest req) {
        BigDecimal deb = BigDecimal.ZERO, cred = BigDecimal.ZERO;
        for (var it : req.itens()) {
            if ("DEBITO".equals(it.tipo())) deb = deb.add(it.valor());
            else cred = cred.add(it.valor());
        }
        if (deb.compareTo(cred) != 0)
            throw new IllegalArgumentException("Partidas dobradas inválidas: D=" + deb + " C=" + cred);

        var empresa = new Empresa(); empresa.setId(req.empresaId());
        var lanc = Lancamento.builder()
                .empresa(empresa).data(req.data())
                .historico(req.historico()).documento(req.documento())
                .build();

        for (var it : req.itens()) {
            var conta = contaRepo.findById(it.contaId()).orElseThrow();
            var item = ItemLancamento.builder()
                    .lancamento(lanc).conta(conta)
                    .tipo(ItemLancamento.Tipo.valueOf(it.tipo()))
                    .valor(it.valor()).build();
            lanc.getItens().add(item);
        }
        return lancRepo.save(lanc);
    }

    public BigDecimal saldo(Long contaId, Long empresaId, LocalDate i, LocalDate f) {
        return itemRepo.saldoConta(contaId, empresaId, i, f);
    }

    public DreResponse dre(Long empresaId, LocalDate ini, LocalDate fim) {
        var contas = contaRepo.findAll();
        Map<Long, BigDecimal> saldos = new HashMap<>();
        for (var c : contas) {
            saldos.put(c.getId(), saldo(c.getId(), empresaId, ini, fim));
        }

        BigDecimal receita = BigDecimal.ZERO, deducoes = BigDecimal.ZERO;
        BigDecimal despesas = BigDecimal.ZERO, cmv = BigDecimal.ZERO;
        Map<String, BigDecimal> dRec = new LinkedHashMap<>();
        Map<String, BigDecimal> dDesp = new LinkedHashMap<>();

        for (var c : contas) {
            BigDecimal s = saldos.getOrDefault(c.getId(), BigDecimal.ZERO);
            if (c.getGrupo() == ContaContabil.Grupo.RECEITA) {
                if (c.getCodigo().startsWith("4.1.01")) deducoes = deducoes.add(s.abs());
                else { receita = receita.add(s.abs()); dRec.put(c.getDescricao(), s.abs()); }
            } else if (c.getGrupo() == ContaContabil.Grupo.DESPESA) {
                if (c.getCodigo().startsWith("5.1.01")) cmv = cmv.add(s.abs());
                else { despesas = despesas.add(s.abs()); dDesp.put(c.getDescricao(), s.abs()); }
            }
        }

        BigDecimal recLiq = receita.subtract(deducoes);
        BigDecimal lucroBruto = recLiq.subtract(cmv);
        BigDecimal lucroOp = lucroBruto.subtract(despesas);
        BigDecimal irpj = lucroOp.multiply(new BigDecimal("0.15"));
        BigDecimal csll = lucroOp.multiply(new BigDecimal("0.09"));
        BigDecimal lucroLiq = lucroOp.subtract(irpj).subtract(csll);

        return new DreResponse(receita, deducoes, recLiq, cmv, lucroBruto,
                despesas, lucroOp, irpj, csll, lucroLiq, dRec, dDesp);
    }
}