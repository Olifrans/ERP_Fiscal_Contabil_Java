package com.senai.escola.service;

import com.senai.escola.entity.Empresa;
import com.senai.escola.entity.NotaFiscal;
import com.senai.escola.repository.EmpresaRepository;
import com.senai.escola.repository.NotaFiscalRepository;
import com.senai.escola.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpedContribuicoesService {

    private final EmpresaRepository empRepo;
    private final NotaFiscalRepository nfRepo;

    public byte[] gerarSpedContribuicoes(LocalDate ini, LocalDate fim) {
        Long eid = TenantContext.get();
        Empresa emp = empRepo.findById(eid).orElseThrow();
        List<NotaFiscal> notas = nfRepo.findByPeriodo(eid, ini, fim);
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("ddMMyyyy");

        StringBuilder sb = new StringBuilder();

        // BLOCO 0: ABERTURA
        sb.append("|0000|020|0|").append(ini.format(dt)).append("|").append(fim.format(dt))
          .append("|").append(emp.getRazaoSocial()).append("|").append(emp.getCnpj().replaceAll("\\D", ""))
          .append("|").append(emp.getInscricaoEstadual() != null ? emp.getInscricaoEstadual() : "")
          .append("|").append(emp.getMunicipio() != null ? emp.getMunicipio() : "SAO PAULO").append("|SP|\n");
        sb.append("|0001|0|\n"); // Abertura do Bloco 0

        // BLOCO 1: ANALÍTICO (Simplificado para NF-e de Saída)
        sb.append("|1001|0|\n"); // Abertura do Bloco 1
        for (NotaFiscal n : notas) {
            if (n.getTipo() == NotaFiscal.Tipo.SAIDA) {
                // Registro C100 (Documento Fiscal)
                sb.append("|C100|0|1|").append(n.getNumero()).append("|")
                  .append(n.getChaveAcesso() != null ? n.getChaveAcesso() : "0").append("|")
                  .append(n.getDataEmissao().format(dt)).append("|").append(n.getValorProdutos()).append("|\n");
                
                // Registro M100 (Crédito PIS/COFINS - Simplificado)
                sb.append("|M100|").append(n.getDataEmissao().format(dt)).append("|")
                  .append(n.getValorPis() != null ? n.getValorPis() : "0").append("|")
                  .append(n.getValorCofins() != null ? n.getValorCofins() : "0").append("|\n");
            }
        }
        sb.append("|1990|").append(notas.size() + 2).append("|\n"); // Encerra Bloco 1

        // BLOCO 9: ENCERRAMENTO
        long totalLinhas = sb.toString().split("\n").length + 2;
        sb.append("|9001|0|\n");
        sb.append("|9900|REG|").append(totalLinhas).append("|\n");
        sb.append("|9999|").append(totalLinhas + 1).append("|\n");

        return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
    }
}