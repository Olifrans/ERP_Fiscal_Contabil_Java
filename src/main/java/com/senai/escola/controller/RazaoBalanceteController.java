package com.senai.escola.controller;




import com.senai.escola.service.RazaoBalanceteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/razao")
@RequiredArgsConstructor
public class RazaoBalanceteController {
    private final RazaoBalanceteService service;

    @GetMapping("/analitico/{contaId}")
    public java.util.List<RazaoBalanceteService.ItemRazao> razao(
            @PathVariable Long contaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.razao(contaId, ini, fim);
    }

    @GetMapping("/balancete")
    public java.util.List<RazaoBalanceteService.ItemBalancete> balancete(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.balancete(ini, fim);
    }
}