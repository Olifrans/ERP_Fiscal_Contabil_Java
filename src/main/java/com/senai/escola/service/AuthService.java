package com.senai.escola.service;


import com.senai.escola.dto.AuthRequest;
import com.senai.escola.dto.AuthResponse;
import com.senai.escola.entity.Usuario;
import com.senai.escola.repository.UsuarioRepository;
import com.senai.escola.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository repo;
    private final JwtTokenProvider jwt;
    private final PasswordEncoder encoder;

    public AuthResponse login(AuthRequest req) {
        Usuario u = repo.findByLogin(req.login())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));
        if (!u.isAtivo()) throw new RuntimeException("Usuário inativo");
        if (!encoder.matches(req.senha(), u.getSenhaHash()))
            throw new RuntimeException("Credenciais inválidas");
        String token = jwt.gerar(u.getLogin(), u.getEmpresaId(), u.getPerfil(), u.getId());
        return new AuthResponse(token, u.getNome(), u.getPerfil(), u.getEmpresaId(), u.getId());
    }

    public Usuario register(Usuario u) {
        if (repo.findByLogin(u.getLogin()).isPresent())
            throw new RuntimeException("Login já existe");
        u.setSenhaHash(encoder.encode(u.getSenhaHash()));
        return repo.save(u);
    }
}