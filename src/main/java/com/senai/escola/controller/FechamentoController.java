package com.senai.escola.controller;





import com.senai.escola.entity.FechamentoMensal;
import com.senai.escola.service.FechamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fechamento")
@RequiredArgsConstructor
public class FechamentoController {
    private final FechamentoService service;

    @PostMapping("/fechar")
    public ResponseEntity<FechamentoMensal> fechar(@RequestParam int ano, @RequestParam int mes) {
        return ResponseEntity.ok(service.fechar(ano, mes));
    }

    @PostMapping("/estornar")
    public ResponseEntity<?> estornar(@RequestParam int ano, @RequestParam int mes) {
        service.estornar(ano, mes);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<FechamentoMensal> listar() { return service.listar(); }
}