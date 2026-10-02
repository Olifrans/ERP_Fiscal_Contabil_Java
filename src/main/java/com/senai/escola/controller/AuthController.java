package com.senai.escola.controller;





import com.erp.fiscal.entity.Usuario;
import com.erp.fiscal.repository.UsuarioRepository;
import com.erp.fiscal.security.JwtTokenProvider;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsuarioRepository repo;
    private final JwtTokenProvider jwt;
    private final PasswordEncoder encoder;

    public record LoginReq(String login, String senha) {}
    public record LoginResp(String token, String nome, String perfil, Long empresaId) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        Usuario u = repo.findByLogin(req.login()).orElse(null);
        if (u == null || !u.isAtivo() || !encoder.matches(req.senha(), u.getSenhaHash()))
            return ResponseEntity.status(401).body("Credenciais inválidas");
        String token = jwt.gerar(u.getLogin(), u.getEmpresaId(), u.getPerfil());
        return ResponseEntity.ok(new LoginResp(token, u.getNome(), u.getPerfil(), u.getEmpresaId()));
    }

    @PostMapping("/register")
    public Usuario register(@RequestBody Usuario u) {
        u.setSenhaHash(encoder.encode(u.getSenhaHash()));
        return repo.save(u);
    }
}