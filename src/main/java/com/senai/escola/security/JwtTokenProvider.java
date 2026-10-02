package com.senai.escola.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long expiration;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expiration-ms}") long exp) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = exp;
    }

    public String gerar(String login, Long empresaId, String perfil) {
        Date now = new Date();
        return Jwts.builder()
                .subject(login)
                .claim("empresaId", empresaId)
                .claim("perfil", perfil)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }

    public boolean valido(String token) {
        try { parse(token); return true; } catch (Exception e) { return false; }
    }

    public String getLogin(String token) { return parse(token).getSubject(); }
    public Long getEmpresaId(String token) { return parse(token).get("empresaId", Long.class); }
    public String getPerfil(String token) { return parse(token).get("perfil", String.class); }
}