package com.senai.escola.service;



import com.senai.escola.entity.*;
import com.senai.escola.repository.*;
import com.senai.escola.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpedEfdService {
    private final EmpresaRepository empRepo;
    private final NotaFiscalRepository nfRepo;
    private final LancamentoRepository lancRepo;

    public byte[] gerar(LocalDate ini, LocalDate fim) {
        Long eid = TenantContext.get();
        Empresa emp = empRepo.findById(eid).orElseThrow();
        DateTimeFormatter d = DateTimeFormatter.ofPattern("ddMMyyyy");

        StringBuilder sb = new StringBuilder();

        // Registro 0000 - Abertura
        sb.append("|0000|003|0|").append(ini.format(d)).append("|").append(fim.format(d))
          .append("|").append(emp.getRazaoSocial())
          .append("|").append(emp.getCnpj().replaceAll("\\D",""))
          .append("|").append(emp.getInscricaoEstadual() != null ? emp.getInscricaoEstadual() : "")
          .append("|").append(emp.getUf() != null ? emp.getUf() : "SP")
          .append("|").append("00")
          .append("|\n");

        // Registro 0001 - Bloco 0
        sb.append("|0001|0|\n");
        sb.append("|0990|").append(3).append("|\n");

        // Registro C100 - NF-e
        sb.append("|C100|0|\n");
        List<NotaFiscal> notas = nfRepo.findByPeriodo(eid, ini, fim);
        int seq = 1;
        for (var n : notas) {
            sb.append("|C170|").append(seq++)
              .append("|").append(n.getCfop() != null ? n.getCfop() : "5102")
              .append("|").append(n.getValorProdutos() != null ? n.getValorProdutos().toPlainString() : "0")
              .append("|").append(n.getValorIcms() != null ? n.getValorIcms().toPlainString() : "0")
              .append("|\n");
        }
        sb.append("|C190|").append(notas.size()).append("|\n");
        sb.append("|C990|").append(notas.size() + 3).append("|\n");

        // Bloco 9 - Encerramento
        long totalLinhas = sb.toString().split("\n").length + 2;
        sb.append("|9001|0|\n");
        sb.append("|9900|REG|").append(totalLinhas).append("|\n");
        sb.append("|9999|").append(totalLinhas + 1).append("|\n");

        return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
    }
}