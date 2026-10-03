package com.senai.escola.audit;



import com.senai.escola.entity.AuditLog;
import com.senai.escola.repository.AuditLogRepository;
import com.senai.escola.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuditListener {
    private final AuditLogRepository repo;
    private final ObjectMapper mapper;

    @PrePersist
    public void prePersist(Object e) { log("CREATE", e); }

    @PreUpdate
    public void preUpdate(Object e) { log("UPDATE", e); }

    @PreRemove
    public void preRemove(Object e) { log("DELETE", e); }

    private void log(String acao, Object e) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String user = (auth != null && auth.getName() != null) ? auth.getName() : "system";
            Long entityId = extrairId(e);
            String payload = mapper.writeValueAsString(e);
            if (payload.length() > 60000) payload = payload.substring(0, 60000) + "...(truncado)";

            AuditLog log = AuditLog.builder()
                    .instante(LocalDateTime.now())
                    .usuario(user)
                    .empresaId(TenantContext.get())
                    .acao(acao)
                    .entidade(e.getClass().getSimpleName())
                    .entityId(entityId)
                    .payload(payload)
                    .build();
            repo.save(log);
        } catch (Exception ignore) {
            // auditoria nunca deve quebrar o fluxo
        }
    }

    private Long extrairId(Object e) {
        try {
            Method m = e.getClass().getMethod("getId");
            Object v = m.invoke(e);
            return v instanceof Number n ? n.longValue() : null;
        } catch (Exception ex) {
            return null;
        }
    }
}