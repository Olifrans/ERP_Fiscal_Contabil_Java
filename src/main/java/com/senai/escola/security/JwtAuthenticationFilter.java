

package com.senai.escola.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            if (jwt.valido(token)) {
                String login = jwt.getLogin(token);
                String perfil = jwt.getPerfil(token);
                Long usuarioId = jwt.getUsuarioId(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        new UsuarioPrincipal(login, usuarioId),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + perfil))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, res);
    }

    public record UsuarioPrincipal(String login, Long id) implements java.security.Principal {
        @Override public String getName() { return login; }
    }
}