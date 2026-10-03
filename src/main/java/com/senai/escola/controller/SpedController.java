package com.senai.escola.controller;




import com.senai.escola.service.SpedEfdService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sped")
@RequiredArgsConstructor
public class SpedController {
    private final SpedEfdService service;

    @GetMapping(path = "/efd", produces = "application/octet-stream")
    public ResponseEntity<byte[]> baixarSped(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ini,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        byte[] data = service.gerar(ini, fim);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sped_efd_" + ini + "_" + fim + ".txt")
                .body(data);
    }
}