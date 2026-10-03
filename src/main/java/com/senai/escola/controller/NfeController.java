package com.senai.escola.controller;




import com.senai.escola.entity.Empresa;
import com.senai.escola.entity.NotaFiscal;
import com.senai.escola.repository.EmpresaRepository;
import com.senai.escola.repository.NotaFiscalRepository;
import com.senai.escola.service.NfeService;
import com.senai.escola.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nfe")
@RequiredArgsConstructor
public class NfeController {
    private final NfeService service;
    private final NotaFiscalRepository nfRepo;
    private final EmpresaRepository empRepo;

    @PostMapping("/gerar/{id}")
    public ResponseEntity<String> gerar(@PathVariable Long id) throws Exception {
        NotaFiscal nf = nfRepo.findById(id).orElseThrow();
        Empresa emp = empRepo.findById(TenantContext.get()).orElseThrow();
        String xml = service.gerarXml(emp, nf);
        String assinado = service.assinar(xml);
        nf.setXml(assinado);
        nf.setStatus("ASSINADA");
        nfRepo.save(nf);
        return ResponseEntity.ok(assinado);
    }

    @GetMapping(value = "/xml/{id}", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> xml(@PathVariable Long id) {
        NotaFiscal nf = nfRepo.findById(id).orElseThrow();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=nfe_" + id + ".xml")
                .body(nf.getXml() != null ? nf.getXml() : "<erro>Sem XML</erro>");
    }
}