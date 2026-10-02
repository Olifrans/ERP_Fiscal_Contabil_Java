package com.senai.escola.tenant;



import com.erp.fiscal.security.JwtTokenProvider;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                Long empresaId = jwt.getEmpresaId(header.substring(7));
                if (empresaId != null) TenantContext.set(empresaId);
            }
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }
}