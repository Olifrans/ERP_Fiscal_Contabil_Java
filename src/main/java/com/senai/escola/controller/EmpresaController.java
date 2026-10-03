package com.senai.escola.controller;




import com.senai.escola.entity.Empresa;
import com.senai.escola.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {
    private final EmpresaRepository repo;

    @GetMapping
    public List<Empresa> listar() { return repo.findAll(); }

    @PostMapping
    public Empresa salvar(@RequestBody Empresa e) { return repo.save(e); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}