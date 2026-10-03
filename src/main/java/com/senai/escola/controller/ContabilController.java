package com.senai.escola.controller;



import com.senai.escola.dto.DreResponse;
import com.senai.escola.dto.LancamentoRequest;
import com.senai.escola.entity.*;
import com.senai.escola.repository.ContaContabilRepository;
import com.senai.escola.service.ContabilService;
import com.senai.escola.tenant.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/contabil")
@RequiredArgsConstructor
public class ContabilController {
    private final ContabilService service;
    private final ContaContabilRepository contaRepo;

    @GetMapping("/contas")
    public List<ContaContabil> contas() {
        return contaRepo.findByEmpresaIdOrderByCodigo(TenantContext.get());
    }

    @PostMapping("/contas")
    public ContaContabil salvarConta(@RequestBody ContaContabil c) {
        c.setEmpresaId(TenantContext.get());
        return contaRepo.save(c);
    }

    @PostMapping("/lancamentos")
    public Lancamento lancar(@Valid @RequestBody LancamentoRequest req) {
        return service.lancar(req);
    }

    @GetMapping("/lancamentos")
    public List<Lancamento> listar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.listar(ini, fim);
    }

    @GetMapping("/dre")
    public DreResponse dre(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.dre(ini, fim);
    }

    @GetMapping("/saldo/{contaId}")
    public ResponseEntity<?> saldo(@PathVariable Long contaId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.saldo(contaId, ini, fim));
    }
}