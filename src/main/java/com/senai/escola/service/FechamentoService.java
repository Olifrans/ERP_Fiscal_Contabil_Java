package com.senai.escola.service;


import com.senai.escola.entity.*;
import com.senai.escola.repository.*;
import com.senai.escola.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FechamentoService {
    private final FechamentoMensalRepository fechRepo;
    private final LancamentoRepository lancRepo;
    private final ItemLancamentoRepository itemRepo;
    private final ContaContabilRepository contaRepo;

    @Transactional
    public FechamentoMensal fechar(int ano, int mes) {
        Long eid = TenantContext.get();
        LocalDate ini = LocalDate.of(ano, mes, 1);
        LocalDate fim = ini.withDayOfMonth(ini.lengthOfMonth());

        if (fechRepo.existsByEmpresaIdAndAnoAndMes(eid, ano, mes))
            throw new IllegalStateException("Período já fechado");

        ContaContabil resultado = contaRepo.findByEmpresaIdAndCodigo(eid, "3.9.01.001")
                .orElseThrow(() -> new IllegalStateException("Crie a conta 3.9.01.001 - Resultado do Exercício"));

        List<ContaContabil> contasResultado = contaRepo.findByEmpresaIdAndGrupoIn(eid,
                List.of(ContaContabil.Grupo.RECEITA, ContaContabil.Grupo.DESPESA));

        Lancamento enc = new Lancamento();
        enc.setEmpresaId(eid);
        enc.setData(fim);
        enc.setHistorico("Encerramento " + ano + "/" + String.format("%02d", mes));
        enc.setDocumento("FECH-" + ano + "-" + String.format("%02d", mes));
        enc.setTipo("FECHAMENTO");
        enc.setItens(new ArrayList<>());

        for (var c : contasResultado) {
            BigDecimal saldo = itemRepo.saldoConta(c.getId(), eid, ini, fim);
            if (saldo.compareTo(BigDecimal.ZERO) == 0) continue;

            ItemLancamento it = new ItemLancamento();
            it.setLancamento(enc);
            it.setConta(c);
            it.setValor(saldo.abs());
            it.setTipo(c.getNatureza() == ContaContabil.Natureza.DEVEDORA
                    ? ItemLancamento.Tipo.CREDITO : ItemLancamento.Tipo.DEBITO);
            enc.getItens().add(it);

            ItemLancamento cp = new ItemLancamento();
            cp.setLancamento(enc);
            cp.setConta(resultado);
            cp.setValor(saldo.abs());
            cp.setTipo(c.getNatureza() == ContaContabil.Natureza.DEVEDORA
                    ? ItemLancamento.Tipo.DEBITO : ItemLancamento.Tipo.CREDITO);
            enc.getItens().add(cp);
        }

        Lancamento salvo = enc.getItens().isEmpty() ? null : lancRepo.save(enc);

        FechamentoMensal f = FechamentoMensal.builder()
                .empresaId(eid).ano(ano).mes(mes)
                .dataFechamento(LocalDate.now())
                .usuario(SecurityContextHolder.getContext().getAuthentication().getName())
                .estornado(false)
                .lancamentoId(salvo != null ? salvo.getId() : null)
                .build();
        return fechRepo.save(f);
    }

    @Transactional
    public void estornar(int ano, int mes) {
        Long eid = TenantContext.get();
        FechamentoMensal f = fechRepo.findByEmpresaIdAndAnoAndMes(eid, ano, mes)
                .orElseThrow(() -> new IllegalStateException("Fechamento não encontrado"));
        if (f.isEstornado()) throw new IllegalStateException("Já estornado");
        lancRepo.deleteByEmpresaIdAndDocumento(eid, "FECH-" + ano + "-" + String.format("%02d", mes));
        f.setEstornado(true);
        fechRepo.save(f);
    }

    public List<FechamentoMensal> listar() {
        return fechRepo.findAll().stream()
                .filter(f -> f.getEmpresaId().equals(TenantContext.get()))
                .toList();
    }
}