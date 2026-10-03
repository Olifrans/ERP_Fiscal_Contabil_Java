package com.senai.escola.controller;


import com.senai.escola.entity.NotaFiscal;
import com.senai.escola.repository.NotaFiscalRepository;
import com.senai.escola.service.FiscalService;
import com.senai.escola.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/fiscal")
@RequiredArgsConstructor
public class FiscalController {
    private final NotaFiscalRepository nfRepo;
    private final FiscalService fiscal;

    @PostMapping("/nf")
    public NotaFiscal salvarNf(@RequestBody NotaFiscal n) {
        n.setEmpresaId(TenantContext.get());
        n.setStatus("DIGITADA");
        return nfRepo.save(n);
    }

    @GetMapping("/nf")
    public java.util.List<NotaFiscal> listar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return nfRepo.findByPeriodo(TenantContext.get(), ini, fim);
    }

    @GetMapping("/apuracao")
    public FiscalService.Apuracao apurar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return fiscal.apurar(ini, fim);
    }
}