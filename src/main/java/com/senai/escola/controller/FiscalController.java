package com.senai.escola.controller;

import com.senai.escola.dto.ProfessorDTO;
import com.senai.escola.entity.Professor;
import com.senai.escola.service.ProfessorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;




package com.erp.fiscal.controller;

import com.erp.fiscal.entity.*;
import com.erp.fiscal.repository.*;
import com.erp.fiscal.service.FiscalService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController @RequestMapping("/api/fiscal") @RequiredArgsConstructor
public class FiscalController {

    private final NotaFiscalRepository nfRepo;
    private final EmpresaRepository empRepo;
    private final FiscalService fiscal;

    @GetMapping("/empresas")
    public java.util.List<Empresa> empresas() { return empRepo.findAll(); }

    @PostMapping("/empresas")
    public Empresa salvarEmp(@RequestBody Empresa e) { return empRepo.save(e); }

    @PostMapping("/nf")
    public NotaFiscal salvarNf(@RequestBody NotaFiscal n) { return nfRepo.save(n); }

    @GetMapping("/apuracao")
    public FiscalService.Apuracao apurar(@RequestParam Long empresaId,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return fiscal.apurar(empresaId, ini, fim);
    }
}