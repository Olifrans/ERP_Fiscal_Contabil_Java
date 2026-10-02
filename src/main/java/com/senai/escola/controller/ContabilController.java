package com.senai.escola.controller;

import com.senai.escola.dto.ProfessorDTO;
import com.senai.escola.entity.Professor;
import com.senai.escola.service.ProfessorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;





import com.erp.fiscal.dto.*;
import com.erp.fiscal.entity.*;
import com.erp.fiscal.repository.*;
import com.erp.fiscal.service.ContabilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController @RequestMapping("/api/contabil") @RequiredArgsConstructor
public class ContabilController {

    private final ContabilService service;
    private final ContaContabilRepository contaRepo;
    private final LancamentoRepository lancRepo;

    @GetMapping("/contas")
    public List<ContaContabil> contas() { return contaRepo.findAll(); }

    @PostMapping("/contas")
    public ContaContabil salvar(@RequestBody ContaContabil c) { return contaRepo.save(c); }

    @PostMapping("/lancamentos")
    public Lancamento lancar(@Valid @RequestBody LancamentoRequest req) {
        return service.lancar(req);
    }

    @GetMapping("/lancamentos")
    public List<Lancamento> listar(@RequestParam Long empresaId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return lancRepo.findByPeriodo(empresaId, ini, fim);
    }

    @GetMapping("/dre")
    public DreResponse dre(@RequestParam Long empresaId,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.dre(empresaId, ini, fim);
    }

    @GetMapping("/saldo/{contaId}")
    public ResponseEntity<?> saldo(@PathVariable Long contaId,
                                   @RequestParam Long empresaId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.saldo(contaId, empresaId, ini, fim));
    }
}