package com.senai.escola.controller;




import com.senai.escola.entity.Empresa;
import com.senai.escola.repository.EmpresaRepository;
import com.senai.escola.service.RazaoBalanceteService;
import com.senai.escola.service.RelatorioPdfService;
import com.senai.escola.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {
    private final RazaoBalanceteService razaoService;
    private final RelatorioPdfService pdfService;
    private final EmpresaRepository empRepo;

    @GetMapping(path = "/balancete/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> balancetePdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) throws Exception {
        var itens = razaoService.balancete(ini, fim);
        Empresa emp = empRepo.findById(TenantContext.get()).orElseThrow();
        byte[] pdf = pdfService.balancetePdf(itens, ini, fim, emp.getRazaoSocial());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balancete.pdf")
                .body(pdf);
    }

    @GetMapping(path = "/razao/pdf/{contaId}", produces = "application/pdf")
    public ResponseEntity<byte[]> razaoPdf(
            @PathVariable Long contaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) throws Exception {
        var itens = razaoService.razao(contaId, ini, fim);
        var conta = razaoService.balancete(ini, fim).stream()
                .filter(b -> b.contaId().equals(contaId)).findFirst().orElseThrow();
        Empresa emp = empRepo.findById(TenantContext.get()).orElseThrow();
        byte[] pdf = pdfService.razaoPdf(itens, conta.codigo() + " - " + conta.descricao(), ini, fim, emp.getRazaoSocial());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=razao_" + contaId + ".pdf")
                .body(pdf);
    }
}